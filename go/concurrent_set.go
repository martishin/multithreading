package main

import (
	"fmt"
	"sync"
)

type ItemSet[T comparable] struct {
	items map[T]bool
	lock  sync.RWMutex
}

func (s *ItemSet[T]) Add(t T) *ItemSet[T] {
	s.lock.Lock()
	defer s.lock.Unlock()
	if s.items == nil {
		s.items = make(map[T]bool)
	}
	_, ok := s.items[t]
	if !ok {
		s.items[t] = true
	}
	return s
}

func (s *ItemSet[T]) Clear() {
	s.lock.Lock()
	defer s.lock.Unlock()
	s.items = make(map[T]bool)
}

func (s *ItemSet[T]) Delete(item T) bool {
	s.lock.Lock()
	defer s.lock.Unlock()
	_, ok := s.items[item]
	if ok {
		delete(s.items, item)
	}
	return ok
}

func (s *ItemSet[T]) Has(item T) bool {
	s.lock.RLock()
	defer s.lock.RUnlock()
	_, ok := s.items[item]
	return ok
}

func (s *ItemSet[T]) Items() []T {
	s.lock.RLock()
	defer s.lock.RUnlock()
	var items []T
	for i := range s.items {
		items = append(items, i)
	}
	return items
}

func (s *ItemSet[T]) Size() int {
	s.lock.RLock()
	defer s.lock.RUnlock()
	return len(s.items)
}

func main() {
	intSet := &ItemSet[int]{}
	var wg sync.WaitGroup

	numGoroutines := 100

	for i := 0; i < numGoroutines; i++ {
		wg.Add(1)
		go func(i int) {
			defer wg.Done()
			intSet.Add(i)
		}(i)
	}

	wg.Wait()

	if intSet.Size() != numGoroutines {
		fmt.Printf("Test failed: expected size %d, got %d\n", numGoroutines, intSet.Size())
	} else {
		fmt.Println("Test passed: all elements added correctly")
	}
}
