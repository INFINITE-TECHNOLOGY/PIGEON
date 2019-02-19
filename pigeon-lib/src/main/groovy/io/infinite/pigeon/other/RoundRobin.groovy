package io.infinite.pigeon.other

class RoundRobin<Type> extends ArrayList<Type> {

    private Integer index = 0

    @Override
    Iterator<Type> iterator() {
        return new Iterator<Type>() {

            @Override
            boolean hasNext() {
                return true
            }

            @Override
            Type next() {
                Type result = RoundRobin.this.get(index) as Type
                index = ((index + 1) % RoundRobin.this.size())
                return result
            }

            @Override
            void remove() {
                throw new Exception("Unable to remove from RoundRobin")
            }

        }
    }

}