package main

import (
	"fmt"
	"math/rand"
	"sync"
	"time"
)

func incrementValue(previous int) int {
	// pretend this is an expensive operation
	time.Sleep(time.Duration(rand.Intn(100)+1) * time.Millisecond)
	return previous + 1
}

func main() {
	noOfConsumers := 10
	var wg sync.WaitGroup
	data := make(chan int)

	// producer
	go func() {
		defer close(data)
		for i := 0; i < 100; i++ {
			data <- incrementValue(i)
		}
	}()

	// consumer

	for i := 0; i < noOfConsumers; i++ {
		wg.Add(1)
		go func(i int) {
			defer wg.Done()
			for data := range data {
				fmt.Printf("Value of i = %d Printed by consumer %d\n", data, i)
			}
		}(i)
	}
	wg.Wait()
}
