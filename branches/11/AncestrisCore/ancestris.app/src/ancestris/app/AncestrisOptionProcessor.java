/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2017 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.app;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.sendopts.CommandException;
import org.netbeans.spi.sendopts.Env;
import org.netbeans.spi.sendopts.Option;
import org.netbeans.spi.sendopts.OptionProcessor;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author daniel
 */
@ServiceProvider(service=OptionProcessor.class)
public class AncestrisOptionProcessor extends OptionProcessor {

    private final Option openOption = Option.defaultArguments();
    private final Option openOption2 = Option.additionalArguments(
                                                'o', "open");

    @Override       
    public Set getOptions() {
        HashSet set = new HashSet();
        set.add(openOption);
        set.add(openOption2);
        return set;
    }

    @Override
    public void process(Env env, Map values) 
                        throws CommandException {
        List<String> filenameList = new ArrayList<String>();
        Object obj = values.get(openOption);
        if (obj != null) {
            filenameList.addAll(Arrays.asList((String[]) obj));
        }
        obj = values.get(openOption2);
        if (obj != null) {
            filenameList.addAll(Arrays.asList((String[]) obj));
        }

        List<File> files = new ArrayList<File>();
        for (String fileName:filenameList){
            File file = new File(fileName);
            if (!file.isAbsolute()) {
                file = new File(env.getCurrentDirectory(),fileName);
            }
            files.add(file);
        }
        StartupFiles.getDefault().setCommandLineFiles(files);
    }
}
