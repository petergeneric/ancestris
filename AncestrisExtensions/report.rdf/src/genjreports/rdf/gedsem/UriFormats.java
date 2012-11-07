package genjreports.rdf.gedsem;

import java.util.HashMap;
import java.util.Map;

public class UriFormats
{
    public static final String DEFAULT_URI = "http://my.domain.com/gedcom/{0}.html";

    public String fam = DEFAULT_URI;
    public String indi = DEFAULT_URI;
    public String obje = DEFAULT_URI;
    public String note = DEFAULT_URI;
    public String repo = DEFAULT_URI;
    public String sour = DEFAULT_URI;
    public String subm = DEFAULT_URI;

    public Map<String, String> getURIs()
    {
        Map<String, String> uris;
        uris = new HashMap<String, String>();
        uris.put("FAM", fam);
        uris.put("INDI", indi);
        uris.put("OBJE", obje);
        uris.put("NOTE", note);
        uris.put("REPO", repo);
        uris.put("SOUR", sour);
        uris.put("SUBM", subm);
        return uris;
    }
}
