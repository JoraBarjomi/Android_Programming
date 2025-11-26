import kotlin.random.Random

fun main() {

    var oleg : Human = Human("Oleg Ivanov", 20)
    oleg.move()

    val people = mutableListOf<Human>()

    var alex : Driver = Driver("Alex", 25)
    for (i in 1..4){
        people.add(Human("Human$i", Random.nextInt(1, 100)))
    }

    val second : Int = 10

    for(i in 1..second){
        for(person in people){
            person.move()
        }
        alex.move()
    }

}