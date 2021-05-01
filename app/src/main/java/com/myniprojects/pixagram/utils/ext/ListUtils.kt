package com.myniprojects.pixagram.utils.ext

fun <T, P> List<T>.mapNeighbours(
    action: (T?, T, T?) -> P
): List<P>
{
    val l = mutableListOf<P>()
    for (i in 0 until size)
    {
        l.add(
            action(
                getOrNull(i - 1),
                get(i),
                getOrNull(i + 1),
            )
        )
    }
    return l
}

fun main()
{
    val l = listOf(1, 2, 3, 4, 5)
    val nl = l.mapNeighbours { previous, current, next ->
        current.plus(previous ?: 0).plus(next ?: 0)
    }
    print(nl) // this should return [3, 6, 9, 12, 9]
}