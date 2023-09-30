package main

import (
	"fmt"
	"math/rand"
	"sync"
	"time"
)

const (
	numTobaccoSmoker    = 1
	numPaperSmoker      = 1
	numMatchesSmoker    = 1
	numRoundsToSimulate = 5
)

var (
	tobaccoSem       = make(chan bool, 1)
	paperSem         = make(chan bool, 1)
	matchesSem       = make(chan bool, 1)
	agentSem         = make(chan bool, 1)
	wg               sync.WaitGroup
	roundsToSimulate = numRoundsToSimulate
)

func main() {

	wg.Add(numTobaccoSmoker + numPaperSmoker + numMatchesSmoker + 1)

	tobaccoSem <- true
	paperSem <- true
	matchesSem <- true
	agentSem <- true

	for i := 0; i < numTobaccoSmoker; i++ {
		go tobaccoSmoker(i)
	}

	for i := 0; i < numPaperSmoker; i++ {
		go paperSmoker(i)
	}

	for i := 0; i < numMatchesSmoker; i++ {
		go matchesSmoker(i)
	}

	go agent()

	wg.Wait()
}

func tobaccoSmoker(id int) {
	for {
		<-tobaccoSem
		<-matchesSem
		fmt.Printf("Курець з тютюном %d скрутив цигарку і курить\n", id)
		time.Sleep(time.Millisecond * time.Duration(rand.Intn(100)))
		tobaccoSem <- true
		matchesSem <- true
	}
}

func paperSmoker(id int) {
	for {
		<-paperSem
		<-matchesSem
		fmt.Printf("Курець з папіром %d скрутив цигарку і курить\n", id)
		time.Sleep(time.Millisecond * time.Duration(rand.Intn(100)))
		paperSem <- true
		matchesSem <- true
	}
}

func matchesSmoker(id int) {
	for {
		<-tobaccoSem
		<-paperSem
		fmt.Printf("Курець з сірниками %d скрутив цигарку і курить\n", id)
		time.Sleep(time.Millisecond * time.Duration(rand.Intn(100)))
		tobaccoSem <- true
		paperSem <- true
	}
}

func agent() {
	for roundsToSimulate > 0 {

		agentSem <- true

		items := rand.Perm(3)
		component1 := items[0]
		component2 := items[1]

		switch component1 {
		case 0:
			fmt.Println("Посередник поклав тютюн на стіл")
			tobaccoSem <- true
		case 1:
			fmt.Println("Посередник поклав папір на стіл")
			paperSem <- true
		case 2:
			fmt.Println("Посередник поклав сірники на стіл")
			matchesSem <- true
		}

		switch component2 {
		case 0:
			fmt.Println("Посередник поклав тютюн на стіл")
			tobaccoSem <- true
		case 1:
			fmt.Println("Посередник поклав папір на стіл")
			paperSem <- true
		case 2:
			fmt.Println("Посередник поклав сірники на стіл")
			matchesSem <- true
		}

		agentSem <- false
		roundsToSimulate--
	}
	wg.Done()
}
