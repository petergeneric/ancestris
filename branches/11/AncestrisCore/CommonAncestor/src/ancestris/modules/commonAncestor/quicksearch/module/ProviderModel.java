/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package ancestris.modules.commonAncestor.quicksearch.module;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import ancestris.modules.commonAncestor.quicksearch.spi.SearchProvider;
import java.util.HashMap;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Dafe Simonek
 */
public final class ProviderModel {

    /** folder in layer file system where provider of fast access content are searched for */
    private static final String SEARCH_PROVIDERS_FOLDER = "/QuickSearch"; //NOI18N
    private static final String COMMAND_PREFIX = "command"; //NOI18N

    //private static ProviderModel instance2;
    static private HashMap<String,ProviderModel> instances = new HashMap<String,ProviderModel>();

    private List<Category> categories;
    
    private HashSet<String> knownCommands;

    private int maxResult = CategoryResult.MAX_RESULTS;
    private int allMaxResult = CategoryResult.ALL_MAX_RESULTS;


    /*
     * Contructor for quicksearch mono instance .
     * Categories and providers must be declared in configuration file parameter /QuickSearch
     * 
     * Example : 
     *  <folder name="QuickSearch">
     *      <folder name="category1">
     *          <attr name="command" stringvalue="A"/>
     *          <attr name="position" intvalue="10"/>
     *          <file name="genjfr-app-searchprovider1.instance">
     *              <attr name="displayName" bundlevalue="genjfr.app.Bundle#QuickSearch/category1/genjfr-app-searchprovider1.instance"/>
     *          </file>
     *      </folder>
     *      <folder name="category2">
     *          <attr name="command" stringvalue="B"/>
     *          <attr name="position" intvalue="20"/>
     *          <file name="genjfr-app-searchprovider2.instance">
     *              <attr name="displayName" bundlevalue="genjfr.app.Bundle#QuickSearch/category2/genjfr-app-searchprovider2.instance"/>
     *          </file>
     *          <file name="genjfr-app-searchprovider3.instance">
     *              <attr name="displayName" bundlevalue="genjfr.app.Bundle#QuickSearch/category2/genjfr-app-searchprovider3.instance"/>
     *          </file>
     *      </folder>
     *      </folder>
     *  </folder>
     * 
     */
    private ProviderModel () {
    }

    /*
     * Contructor for quicksearch multi instances in same module
     * 
     * @param categoryName
     * @param categoryDisplayName
     * @param searchProvider 
     */
    public ProviderModel (String categoryName, String categoryDisplayName, 
            SearchProvider searchProvider, String commandPrefix,
            int maxResult, int allMaxResult ) {
        Category category = new Category(categoryName, categoryDisplayName, commandPrefix);
        category.addSearchProvider(searchProvider);
        categories = new ArrayList<ProviderModel.Category>();
        categories.add(category);
        this.maxResult = maxResult;
        this.allMaxResult = allMaxResult;

    }

     /*
     * 
     */
    public static ProviderModel getInstance () {
        ProviderModel providerModel = instances.get(SEARCH_PROVIDERS_FOLDER);
        if ( providerModel == null ) {
            providerModel = new ProviderModel();
            instances.put(SEARCH_PROVIDERS_FOLDER, providerModel);
        }
        return providerModel;
    }

    /**
     * Get the value of categories
     *
     * @return the value of categories
     */
    public List<Category> getCategories() {
        if (categories == null) {
            categories = loadCategories();
        }
        return this.categories;
    }
    
    public boolean isKnownCommand(String command) {
        if (knownCommands == null) {
            knownCommands = new HashSet<String>();
            for (Category cat : getCategories()) {
                knownCommands.add(cat.getCommandPrefix());
            }
        }
        return knownCommands.contains(command);
    }

    int getAllMaxResult() {
        return allMaxResult;
    }

    int getMaxResult() {
        return maxResult;
    }

    public static class Category {

        private FileObject fo;
        private String name;

        private String displayName, commandPrefix;
        
        private List<SearchProvider> providers;

        /**
         * Constructor ofr quicksearch mono instance in a module
         * Category name is given in fileObject parameter
         * @param fo  
         * @param displayName
         * @param commandPrefix 
         */
        public Category(FileObject fo, String displayName, String commandPrefix) {
            this.fo = fo;
            this.name = fo.getNameExt();
            this.displayName = displayName;
            this.commandPrefix = commandPrefix;
        }

        /**
         * Construtor for quickserach multi instances in a module
         * Category name is given in the first parameter
         * @param name
         * @param displayName
         * @param commandPrefix 
         */
        public Category(String name, String displayName, String commandPrefix) {
            this.fo = null;
            this.name = name;
            this.displayName = displayName;
            this.commandPrefix = commandPrefix;
        }

        public List<SearchProvider> getProviders () {
            if (providers == null) {
                Collection<? extends SearchProvider> catProviders =
                        Lookups.forPath(fo.getPath()).lookupAll(SearchProvider.class);
                providers = new ArrayList<SearchProvider>(catProviders);
            }

            return providers;
        }
        
        public String getName() {
            return name;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getCommandPrefix() {
            return commandPrefix;
        }
        
        public void addSearchProvider(SearchProvider searchProvider) {
            if (providers == null ) {
                providers = new ArrayList<SearchProvider>();
            }
            providers.add(searchProvider);
        }

    } // end of Category

    private List<Category> loadCategories () {
        FileObject[] categoryFOs = FileUtil.getConfigFile(SEARCH_PROVIDERS_FOLDER).getChildren();

        // respect ordering defined in layers
        List<FileObject> sortedCats = FileUtil.getOrder(Arrays.asList(categoryFOs), false);

        List<ProviderModel.Category> categoriesList = new ArrayList<ProviderModel.Category>(sortedCats.size());

        for (FileObject curFO : sortedCats) {
            String displayName = null;
            try {
                displayName = curFO.getFileSystem().getDecorator().annotateName(
                        curFO.getNameExt(), Collections.singleton(curFO));
            } catch (FileStateInvalidException ex) {
                Logger.getLogger(ProviderModel.class.getName()).log(Level.WARNING,
                        "Obtaining display name for " + curFO + " failed.", ex);
            }

            String commandPrefix = null;
            Object cpAttr = curFO.getAttribute(COMMAND_PREFIX);
            if (cpAttr instanceof String) {
                commandPrefix = (String)cpAttr;
            }

            categoriesList.add(new Category(curFO, displayName, commandPrefix));
        }

        return categoriesList;
    }

}
