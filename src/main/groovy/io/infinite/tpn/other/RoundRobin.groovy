package io.infinite.tpn.other

import io.infinite.blackbox.BlackBox
import io.infinite.blackbox.BlackBoxLevel

class RoundRobin<Type> extends ArrayList<Type> {

    private Integer index = 0

    @BlackBox(blackBoxLevel = BlackBoxLevel.EXPRESSION)
    @Override
    Iterator<Type> iterator() {
        return new Iterator<Type>() {

            @BlackBox(blackBoxLevel = BlackBoxLevel.EXPRESSION)
            @Override
            boolean hasNext() {
                return true
            }

            @BlackBox(blackBoxLevel = BlackBoxLevel.EXPRESSION)
            @Override
            Type next() {
                Type result = get(index)
                index = ((index + 1) % size())
                return result
            }

            @BlackBox(blackBoxLevel = BlackBoxLevel.EXPRESSION)
            @Override
            void remove() {
                throw new Exception("Unable to remove from RoundRobin")
            }

        }
    }

}