package main

import (
	"fmt"
	"sync"
	"sync/atomic"
)

func main() {
	noOfProducer := 10
	data := make(chan int64)
	var wg sync.WaitGroup

	// producer
	var ops int64
	for i := 0; i < noOfProducer; i++ {
		wg.Add(1)
		go func() {
			for c := 0; c < 100; c++ {
				atomic.AddInt64(&ops, 1)
				data <- atomic.LoadInt64(&ops)
			}
			wg.Done()
		}()
	}

	go func() {
		wg.Wait()
		close(data)
	}()

	// consumer
	for data := range data {
		fmt.Printf("Value of i = %d\n", data)
	}
}
