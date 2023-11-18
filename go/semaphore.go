package main

import (
	"errors"
	"fmt"
	"sync"
	"time"
)

var (
	ErrNoTickets      = errors.New("could not acquire semaphore")
	ErrIllegalRelease = errors.New("can't release the semaphore without acquiring it first")
)

// Interface contains the behavior of a semaphore that can be acquired and/or released.
type Semaphore interface {
	Acquire() error
	Release() error
}

type semaphore struct {
	sem     chan struct{}
	timeout time.Duration
}

func (s *semaphore) Acquire() error {
	select {
	case s.sem <- struct{}{}:
		return nil
	case <-time.After(s.timeout):
		return ErrNoTickets
	}
}

func (s *semaphore) Release() error {
	select {
	case _ = <-s.sem:
		return nil
	case <-time.After(s.timeout):
		return ErrIllegalRelease
	}
}

func New(tickets int, timeout time.Duration) Semaphore {
	return &semaphore{
		sem:     make(chan struct{}, tickets),
		timeout: timeout,
	}
}

func main() {
	tickets, timeout := 1, 3*time.Second
	s := New(tickets, timeout)

	var wg sync.WaitGroup
	workerCount := 5

	for i := 1; i <= workerCount; i++ {
		wg.Add(1)
		go func(workerID int) {
			defer wg.Done()

			fmt.Printf("Worker %d attempting to acquire semaphore...\n", workerID)
			if err := s.Acquire(); err != nil {
				fmt.Printf("Worker %d failed to acquire semaphore: %s\n", workerID, err)
				return
			}

			fmt.Printf("Worker %d acquired semaphore\n", workerID)
			// Simulating some work
			time.Sleep(1 * time.Second)
			fmt.Printf("Worker %d releasing semaphore\n", workerID)

			if err := s.Release(); err != nil {
				fmt.Printf("Worker %d failed to release semaphore: %s\n", workerID, err)
				return
			}
		}(i)
	}

	wg.Wait()
	fmt.Println("All workers completed")
}
