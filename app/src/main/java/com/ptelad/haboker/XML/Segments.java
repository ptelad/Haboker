package com.ptelad.haboker.XML;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Root(strict = false)
public class Segments {
    @ElementList(entry="Segment", inline = true)
    public List<Segment> list;
}

