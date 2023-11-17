package main

import (
	"fmt"
	"time"
)

func increment(previous int) int {
	// pretend this is an expensive operation
	time.Sleep(1 * time.Millisecond)
	return previous + 1
}

func main() {
	data := make(chan int)

	// producer
	go func() {
		defer close(data)
		for i := 0; i < 1000; i++ {
			data <- increment(i)
		}
	}()

	// consumer
	for i := range data {
		fmt.Printf("Value of i = %d\n", i)
	}
}
