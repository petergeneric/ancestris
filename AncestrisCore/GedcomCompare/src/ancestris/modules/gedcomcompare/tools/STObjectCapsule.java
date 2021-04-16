/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2020 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.gedcomcompare.tools;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author frederic
 */
public class STObjectCapsule implements Serializable {

    public String spaceTime = "";

    public int nbB = 0;
    public int nbM = 0;
    public int nbS = 0;
    public int nbZ = 0;
    public int nbCities = 0;
    public int nbNames = 0;
    public int nbYears = 0;
    public int nbCityName = 0;
    public int nbEvents = 0;
    public Double lat = 0d;
    public Double lon = 0d;

    public Set<String> cities = new HashSet<>();
    public Set<String> names = new HashSet<>();
    public Set<String> years = new HashSet<>();

    public List<STEventCapsule> events = new ArrayList<>();

    public STObjectCapsule() {
    }
}
