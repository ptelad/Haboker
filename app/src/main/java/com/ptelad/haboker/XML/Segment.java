package com.ptelad.haboker.XML;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

public @Root(strict = false)
class Segment {
    @Element(data = true)
    public String RecordedProgramsName;

    @Element(data = true)
    public String RecordedProgramsDownloadFile;

    @Element(data = true)
    public String RecordedProgramsImg;
}
