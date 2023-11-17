package main

import (
	"fmt"
	"log"
	"time"
)

func main() {
	goChannel1 := make(chan string)
	goChannel2 := make(chan string)
	go fibonacciOne(goChannel1)
	go fibonacciTwo(goChannel2)
	printAllNumbers(goChannel1, goChannel2)
}

func printAllNumbers(goChannel1, goChannel2 chan string) {
	timer := time.After(time.Second * 5)
	start := time.Now()
	for {
		select {
		case n := <-goChannel1:
			fmt.Println(n)
			elapsed := time.Since(start)
			log.Printf("Binomial took %s", elapsed)
		case n := <-goChannel2:
			fmt.Println(n)
			elapsed := time.Since(start)
			log.Printf("Binomial took %s", elapsed)
		case <-timer:
			fmt.Println("Timeout: Fibonacci numbers finished")
			return
		}
	}
}

func fibonacciOne(ch chan string) {
	x, y := 0, 1
	for ; x < 100000; x, y = x+y, x {
		time.Sleep(time.Millisecond * 400)
		ch <- fmt.Sprintf("Fibonacci Number from goroutine 1: %d", x)
	}
}

func fibonacciTwo(ch chan string) {
	x, y := 0, 1
	for ; x < 100000; x, y = x+y, x {
		time.Sleep(time.Millisecond * 600)
		ch <- fmt.Sprintf("Fibonacci Number from goroutine 2: %d", x)
	}
}
