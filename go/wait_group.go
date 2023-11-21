package main

import (
	"fmt"
	"net/http"
	"sync"
)

var urls = []string{
	"https://educative.io",
	"https://educative.io/teach",
	"https://educative.io/assessments",
	"https://educative.io/projects",
	"https://educative.io/paths",
	"https://educative.io/learning-plans",
	"https://educative.io/learn",
	"https://educative.io/edpresso",
	"https://educative.io/explore",
	"https://educative.io/efer-a-friend",
	"https://google.com",
	"https://twitter.com",
}

func fetchUrlWithWaitGroup(url string, wg *sync.WaitGroup) {
	defer wg.Done()
	resp, err := http.Get(url)
	if err != nil {
		fmt.Println(err)
	}
	fmt.Println(resp.Status)
}

func fetchUrl(url string) {
	resp, err := http.Get(url)
	if err != nil {
		fmt.Println(err)
	}
	fmt.Println(resp.Status)
}

func homeHandler(w http.ResponseWriter, r *http.Request) {
	var wg sync.WaitGroup
	fmt.Println("Home endpoint")
	wg.Add(len(urls))
	for _, url := range urls {
		url := url

		go func() {
			defer wg.Done()
			fetchUrl(url)
		}()
	}
	wg.Wait()
	fmt.Println("All Response received successfully")
	fmt.Fprintf(w, "All Response received successfully")
}

func handleRequests() {
	http.HandleFunc("/", homeHandler)
	http.ListenAndServe(":7079", nil)
}

func main() {
	handleRequests()
}
