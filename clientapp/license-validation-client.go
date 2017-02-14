package main

import (
	"html/template"
	"log"
	"net/http"
)

func index(w http.ResponseWriter, r *http.Request) {
	t, _ := template.ParseFiles("license-validation-client.html")
	t.Execute(w, nil)
}

func main() {
	http.HandleFunc("/", index)
	log.Println("App running on http://localhost:8181")
	log.Fatal(http.ListenAndServe("localhost:8181", nil))
}
