package main

import (
	"fmt"
	"sync"
	"time"
)

func read(sharedMap map[int]int, mu *sync.Mutex) {
	for {
		mu.Lock()
		val := sharedMap[0]
		fmt.Println(val)
		mu.Unlock()
	}
}

func write(sharedMap map[int]int, mu *sync.Mutex) {
	for {
		mu.Lock()
		sharedMap[0] = sharedMap[0] + 1
		mu.Unlock()
	}
}

func main() {
	var mu sync.Mutex
	sharedMap := make(map[int]int)
	sharedMap[0] = 0

	go read(sharedMap, &mu)
	go write(sharedMap, &mu)
	time.Sleep(100 * time.Millisecond)
}
