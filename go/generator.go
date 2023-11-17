package main

import (
	"fmt"
	"math/rand"
)

func main() {
	for returnedVal := range randomGenerator(10) {
		fmt.Println(returnedVal)
	}
}

func randomGenerator(n int) <-chan int {
	output := make(chan int)
	go func() {
		defer close(output)

		fmt.Println("Hello, I am the producer")
		for i := 0; i < n; i++ {
			output <- rand.Intn(n) + 1
		}
	}()
	return output
}
