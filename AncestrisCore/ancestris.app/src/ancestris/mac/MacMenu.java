package ancestris.mac;

/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2016 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */



import java.util.logging.Logger;
import org.openide.util.Exceptions;

import com.apple.eawt.Application;
import com.apple.eawt.ApplicationAdapter;
import com.apple.eawt.ApplicationEvent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

/**
 * By default, menu appears as:
 * 
 * -----------------
 *  ancestris           <== title bar
 * -----------------
 *  About ancestris
 * -----------------
 *  Preferences
 * -----------------
 *  Services
 * -----------------
 *  Hide ancestris
 *  Hide Others
 *  Show All
 * -----------------
 *  Quit ancestris
 * -----------------
  * 
 * @author frederic
 */
public class MacMenu {

    private Logger LOG;
    
    public MacMenu(Logger log) {
        this.LOG = log;
        LOG.info("*** DEBUG *** - Defining MacMenu");
    }
        
    public void setup() {
        try {
            LOG.info("*** DEBUG *** - Setting up Mac Menu");
            
            // Set some mac-specific properties
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Ancestris");   // has no effect
            System.setProperty("dock:name", "Ancestris");   // has no effect
            
            // Create an instance of the Mac Application class
            Application macApplication = Application.getApplication();

            
            // Enable/Disable items
            macApplication.setEnabledAboutMenu(true); // This works
            macApplication.setEnabledPreferencesMenu(true); // This works
            macApplication.removeAboutMenuItem();  // This works
            macApplication.removePreferencesMenuItem(); //test

            // test
            JMenuBar jmb = new JMenuBar();
            JMenu fileMenu = new JMenu("Fichier");
            JMenuItem menuItem = new JMenuItem("Hello world");
            fileMenu.add(menuItem);
            jmb.add(fileMenu);
            macApplication.setDefaultMenuBar(jmb);  
            
            // Handles Quit and About
            macApplication.addApplicationListener(new ApplicationAdapter() {
                @Override
                public void handleQuit(ApplicationEvent event) {
                    JOptionPane.showMessageDialog(null,
                            "prefs Ancestris 1",
                            "prefs Ancestris 2",
                            JOptionPane.INFORMATION_MESSAGE);                
                }
                @Override
                public void handleAbout(ApplicationEvent event) {
                    JOptionPane.showMessageDialog(null,
                            "about Ancestris 1",
                            "about Ancestris 2",
                            JOptionPane.INFORMATION_MESSAGE);                }
            });
                
            
            
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        } catch (Throwable ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    
    

            

}


//        try {
//            Toolkit tk = Toolkit.getToolkit();
//
//            TKSystemMenu systemMenu = tk.getSystemMenu();
//            LOG.info("*** DEBUG *** - systemMenu = " + systemMenu);
//            if (systemMenu == null) { // || !systemMenu.isSupported()) {
//                return;
//            }
//
//            Field field = systemMenu.getClass().getDeclaredField("glassSystemMenuBar");
//            LOG.info("*** DEBUG *** - field = " + field);
//            field.setAccessible(true);
//            
//            MethodHandle getSystemMenuBar = MethodHandles.lookup().unreflectGetter(field);
//            LOG.info("*** DEBUG *** - getSystemMenuBar = " + getSystemMenuBar);
//            
//            MethodHandle setSystemMenuBar = MethodHandles.lookup().unreflectSetter(field);
//            LOG.info("*** DEBUG *** - setSystemMenuBar = " + setSystemMenuBar);
//            
//            Method method = systemMenu.getClass().getDeclaredMethod("insertMenu", com.sun.glass.ui.Menu.class, MenuBase.class, int.class);
//            LOG.info("*** DEBUG *** - method = " + method);
//            method.setAccessible(true);
//            
//            MethodHandle insertMenu = MethodHandles.lookup().unreflect(method);
//            LOG.info("*** DEBUG *** - insertMenu = " + insertMenu);
//
//            final Application applicationAdapter = PlatformFactory.getPlatformFactory().createApplication();//Application.GetApplication();
//            LOG.info("*** DEBUG *** - applicationAdapter = " + applicationAdapter);
//            if (applicationAdapter == null) {
//                return;
//            }
//
//            
////            Method[] methods = applicationAdapter.getClass().getDeclaredMethods();
////            final Method hide = applicationAdapter.getClass().getDeclaredMethod("_hide");
////            LOG.info("*** DEBUG *** - hide = " + hide);
////            hide.setAccessible(true);
////            MethodHandles.lookup().unreflect(hide);
////            
////            final Method hideOtherApplications = applicationAdapter.getClass().getDeclaredMethod("_hideOtherApplications");
////            hideOtherApplications.setAccessible(true);
////            MethodHandles.lookup().unreflect(hideOtherApplications);
////
////            final Method unhideAllApplications = applicationAdapter.getClass().getDeclaredMethod("_unhideAllApplications");
////            unhideAllApplications.setAccessible(true);
////            MethodHandles.lookup().unreflect(unhideAllApplications);
//
//            
//            // Create the default Application menu
//            String appName = "Ancestris";
//
//            MenuItem quitItem = new MenuItem("Quitter " + appName);
//            quitItem.setOnAction(new EventHandler<ActionEvent>() {
//                @Override
//                public void handle(ActionEvent e) {
//                    Application.EventHandler eh = applicationAdapter.getEventHandler();
//                    if (eh != null) {
//                        eh.handleQuitAction(Application.GetApplication(), System.nanoTime());
//                    }
//                }
//            });
//            quitItem.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.META_DOWN));
//
//            
//            MenuItem hideItem = new MenuItem("Cacher " + appName);
//            hideItem.setOnAction(new EventHandler<ActionEvent>() {
//                @Override
//                public void handle(ActionEvent e) {
//                    Application.EventHandler eh = applicationAdapter.getEventHandler();
//                    if (eh != null) {
//                        try {
//                            //hide.invoke(applicationAdapter);
//                        } catch (Exception ex) {
//                            Exceptions.printStackTrace(ex);
//                        }
//                    }
//                }
//            });
//            hideItem.setAccelerator(new KeyCodeCombination(KeyCode.H, KeyCombination.META_DOWN));
//
//            
//            MenuItem hideOthersItem = new MenuItem("Cacher les autres");
//            hideOthersItem.setOnAction(new EventHandler<ActionEvent>() {
//                @Override
//                public void handle(ActionEvent e) {
//                    Application.EventHandler eh = applicationAdapter.getEventHandler();
//                    if (eh != null) {
//                        try {
//                            //hideOtherApplications.invoke(applicationAdapter);
//                        } catch (Exception ex) {
//                            Exceptions.printStackTrace(ex);
//                        }
//                    }
//                }
//            });
//            hideOthersItem.setAccelerator(new KeyCodeCombination(KeyCode.H, KeyCombination.META_DOWN, KeyCombination.ALT_DOWN));
//            
//            MenuItem unhideAllItem = new MenuItem("Tout montrer");
//            unhideAllItem.setOnAction(new EventHandler<ActionEvent>() {
//                @Override
//                public void handle(ActionEvent e) {
//                    Application.EventHandler eh = applicationAdapter.getEventHandler();
//                    if (eh != null) {
//                        try {
//                            //unhideAllApplications.invoke(applicationAdapter);
//                        } catch (Exception ex) {
//                            Exceptions.printStackTrace(ex);
//                        }
//                    }
//                }
//            });
//            
//            Menu defaultApplicationMenu = new Menu(appName, null);
//            defaultApplicationMenu.getItems().add(hideItem);
//            defaultApplicationMenu.getItems().add(hideOthersItem);
//            defaultApplicationMenu.getItems().add(unhideAllItem);
////            defaultApplicationMenu.getItems().add(new SeparatorMenuItem());
//            defaultApplicationMenu.getItems().add(quitItem);
//        
//            // Update the existing Application menu
//            LOG.info("*** DEBUG *** - update existing menu");
////            MenuBar glassSystemMenuBar = (MenuBar) getSystemMenuBar.invoke(systemMenu);
////            LOG.info("*** DEBUG *** - glassSystemMenuBar = " + glassSystemMenuBar);
////            if (glassSystemMenuBar == null) {
////                setSystemMenuBar.invoke(new Object[] {systemMenu, applicationAdapter.createMenuBar()});
////            } else {
////                removeMenu(glassSystemMenuBar, 0);
////            }
//            LOG.info("*** DEBUG *** - insertMenu menu");
//            insertMenu.invoke(new Object[] {systemMenu, null, GlobalMenuAdapter.adapt(defaultApplicationMenu), 0});
//
//            // Since we now have a reference to the menu, we can rename items
//            defaultApplicationMenu.getItems().get(1).setText("Hide all the others");
//            LOG.info("*** DEBUG *** - END************");
//            
//        } catch (Exception ex) {
//            Exceptions.printStackTrace(ex);
//        } catch (Throwable ex) {
//            Exceptions.printStackTrace(ex);
//        }
//
//        // end
//
    
    
    
//	private void removeMenu(MenuBar glassSystemMenuBar, int index) {
//		if (glassSystemMenuBar.getMenus().size() <= index) {
//			return;
//		}
//		clearMenu(glassSystemMenuBar.getMenus().get(index));
//		glassSystemMenuBar.remove(index);
//	}
//
//	private void clearMenu(com.sun.glass.ui.Menu menu) {
//		for (int i = menu.getItems().size() - 1; i >= 0; i--) {
//			Object o = menu.getItems().get(i);
//
//			if (o instanceof MenuItem) {
//				((com.sun.glass.ui.MenuItem) o).setCallback(null);
//			} else if (o instanceof com.sun.glass.ui.MenuItem) {
//				clearMenu((com.sun.glass.ui.Menu) o);
//			}
//		}
//		menu.setEventHandler(null);
//	}
