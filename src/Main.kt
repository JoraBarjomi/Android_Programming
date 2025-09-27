import kotlin.random.Random

class Human{
    var name: String = ""
    var age: Int = 0
    val velocity: Int = Random.nextInt(0, 5)

    var x : Int = 0
    var y : Int = 0

    constructor(_name: String, _age: Int){
        name = _name
        age = _age
    }

    fun move(){

        val rnd = velocity * Random.nextInt(-1, 2)
        val rnd2 = velocity * Random.nextInt(-1, 2)

        x += rnd
        y += rnd2

        println("$name is moved {$x, $y}")

    }
}

fun main() {

    val rnd = Random.nextInt(0, 10)

    var oleg : Human = Human("Oleg Ivanov", 20)
    oleg.move()

    val people = mutableListOf<Human>()

    for (i in 1..26){
        people.add(Human("Human$i", Random.nextInt(1, 100)))
    }

    val second : Int = 200

//    for(i in 1..second){
//        people[Random.nextInt(0, 26)].move()
//    }
    for(i in 1..second){
        for(person in people){
            person.move()
        }
    }

}