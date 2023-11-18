package main

import (
	"log"
	"time"
)

func main() {
	iterationCount := 5
	done := make(chan interface{})
	stringStream := make(chan interface{})
	go func(done chan interface{}) {
		for i := 0; i < iterationCount; i++ {
			select {
			case <-done:
			case stringStream <- i:
			}
		}
	}(done)

	out1, out2 := Tee(done, stringStream)
	var loggerCount, printerCount *int
	logger := func(done, c <-chan interface{}) {
		for v := range orDone(done, c) {
			if loggerCount == nil {
				init := 0
				loggerCount = &init
			}

			sum := *loggerCount + 1
			loggerCount = &sum
			log.Printf("Invoke logger with value %d", v.(int))
		}
	}

	printer := func(done, c <-chan interface{}) {
		for v := range orDone(done, c) {
			if printerCount == nil {
				init := 0
				printerCount = &init
			}

			sum := *printerCount + 1
			printerCount = &sum
			log.Printf("Invoke printer with value %d", v.(int))
		}
	}

	go logger(done, out1)
	go printer(done, out2)
	go func() {
		time.Sleep(time.Second)
		if *loggerCount != iterationCount {
			log.Fatalf("failed, mismatch loggerCount, is %d expected %d ", *loggerCount, iterationCount)
		} else {
			log.Printf("logger success")
		}

		if *printerCount != iterationCount {
			log.Fatalf("failed, mismatch printerCount, is %d expected %d ", *printerCount, iterationCount)
		} else {
			log.Printf("printer success")
		}
		close(done)
	}()

	<-done
}

func Tee(
	done,
	c chan interface{}) (<-chan interface{}, <-chan interface{}) {
	out1 := make(chan interface{})
	out2 := make(chan interface{})

	go func() {
		defer close(out1)
		defer close(out2)
		for val := range orDone(done, c) {
			var out1, out2 = out1, out2
			for i := 0; i < 2; i++ {
				select {
				case <-done:
				case out1 <- val:
					out1 = nil
				case out2 <- val:
					out2 = nil
				}
			}
		}
	}()

	return out1, out2
}

func orDone(
	done,
	c <-chan interface{}) <-chan interface{} {
	resultStream := make(chan interface{})

	go func() {
		for {
			select {
			case <-done:
			case v, ok := <-c:
				if !ok {
					return
				}
				select {
				case <-done:
				case resultStream <- v:
				}
			}
		}
	}()

	return resultStream
}
