package com.example.birdview

class BirdSpecieCategoryModel {
    var id:String = ""
    var specie:String =""
    var amountCategory = ""
    var uid:String =""

    constructor()
    constructor(id:String, specie: String,amountCategory : String, uid: String){
        this.id = id
        this.specie = specie
        this.amountCategory = amountCategory
        this.uid = uid
    }
}