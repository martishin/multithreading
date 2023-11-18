package main

import "fmt"

// The 'chan<-' syntax indicates that 'pings' is a send-only (write only) channel.
func ping(pings chan<- string, msg string) {
	pings <- msg
}

// The '<-chan' syntax indicates that 'pings' is a receive-only (read only) channel
// and 'chan<-' syntax indicates that 'pongs' is a send-only (write only) channel.
func pong(pings <-chan string, pongs chan<- string) {
	msg := <-pings
	pongs <- msg
}

func main() {
	pings := make(chan string, 1)
	pongs := make(chan string, 1)
	ping(pings, "ping")
	pong(pings, pongs)
	fmt.Println(<-pongs)
}
