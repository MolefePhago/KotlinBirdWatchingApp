package com.example.birdview

class SpecieViewModel {
    var uid: String = ""
    var id: String = ""
    var Name: String = ""
    var Description: String = ""
    var categoryId: String = ""
    var Date: String = ""
    var Location: String = ""
    var BirdImage = ""
    var lat: Double = 0.0
    var lng: Double = 0.0

    constructor()

    constructor(
        uid: String,
        id: String,
        Name: String,
        Description: String,
        categoryId: String,
        Date: String,
        Location: String,
        BirdImage: String,
        lat: Double,
        lng: Double
    ) {
        this.uid = uid
        this.id = id
        this.Name = Name
        this.Description = Description
        this.categoryId = categoryId
        this.Date = Date
        this.Location = Location
        this.BirdImage = BirdImage
        this.lat = lat
        this.lng = lng
    }
}
