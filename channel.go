package main

import (
	"fmt"
)

func next(n int) chan int {
	out := make(chan int)
	go func() {
		defer close(out)
		i, j := 0, 1
		for k := 0; k < n; k++ {
			out <- i
			i, j = i+j, i
		}
	}()
	return out
}

func useFibo(channel chan<- int, n int) {
	for val := range next(n) {
		channel <- val
	}
}

func main() {
	fiboNum := 10
	channel := make(chan int)
	go useFibo(channel, fiboNum)

	for i := 0; i < fiboNum; i++ {
		fmt.Println(<-channel)
	}
}
