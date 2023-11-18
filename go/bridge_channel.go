package main

import (
	"fmt"
)

func main() {
	orDone := func(done, c <-chan interface{}) <-chan interface{} {
		valStream := make(chan interface{})
		go func() {
			defer close(valStream)
			for {
				select {
				case <-done:
					return
				case v, ok := <-c:
					if ok == false {
						return
					}
					select {
					case valStream <- v:
					case <-done:
					}
				}
			}
		}()
		return valStream
	}
	bridge := func(
		done <-chan interface{},
		chanStream <-chan <-chan interface{},
	) <-chan interface{} {
		valStream := make(chan interface{}) // <1> This is the channel that will return all values from bridge.
		go func() {
			defer close(valStream)
			for { // <2> This loop is responsible for pulling channels off of chanStream and providing them to a nested loop for use.
				var stream <-chan interface{}
				select {
				case maybeStream, ok := <-chanStream:
					if ok == false {
						return
					}
					stream = maybeStream
				case <-done:
					return
				}
				for val := range orDone(done, stream) { // <3> This loop is responsible for reading values off the channel it has been given and repeating those values onto valStream. When the stream we’re currently looping over is closed, we break out of the loop performing the reads from this channel, and continue with the next iteration of the loop, selecting channels to read from. This provides us with an unbroken stream of values.
					select {
					case valStream <- val:
					case <-done:
					}
				}
			}
		}()
		return valStream
	}
	genVals := func() <-chan <-chan interface{} {
		chanStream := make(chan (<-chan interface{}))
		go func() {
			defer close(chanStream)
			for i := 0; i < 10; i++ {
				stream := make(chan interface{}, 1)
				stream <- i
				close(stream)
				chanStream <- stream
			}
		}()
		return chanStream
	}

	for v := range bridge(nil, genVals()) {
		fmt.Printf("%v ", v)
	}
}
