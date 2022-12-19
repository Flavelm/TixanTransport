package net.toeba.tixantransport;

import java.util.ArrayList;
import java.util.List;

public class Queue<T>
{
    private final List<T> Elements;

    public Queue()
    {
        Elements = new ArrayList<>();
    }

    public void Add(T element)
    {
        Elements.add(element);
    }

    public T Next()
    {
        if (Elements.isEmpty()) { return null; }

        T ToReturn = Elements.get(0);
        Elements.remove(0);
        return ToReturn;
    }
}
