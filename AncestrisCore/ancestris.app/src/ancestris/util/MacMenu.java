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

package ancestris.util;

import com.sun.glass.ui.Application;
import com.sun.glass.ui.MenuBar;
import com.sun.javafx.menu.MenuBase;
import com.sun.javafx.scene.control.GlobalMenuAdapter;
import com.sun.javafx.tk.TKSystemMenu;
import com.sun.javafx.tk.Toolkit;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import org.openide.util.Exceptions;

/**
 *
 * @author frederic
 */
public class MacMenu {
    
    public MacMenu() {
        System.out.println("*** DEBUG *** - Defining MacMenu");
    }
        
    public void setUp() {
        System.out.println("*** DEBUG *** - Setting up Mac Menu");
        if (true) return;
        
        try {
            Toolkit tk = Toolkit.getToolkit();

            TKSystemMenu systemMenu = tk.getSystemMenu();
            System.out.println("*** DEBUG *** - systemMenu = " + systemMenu);
            if (systemMenu == null || !systemMenu.isSupported()) {
                return;
            }

            Field field = systemMenu.getClass().getDeclaredField("glassSystemMenuBar");
            System.out.println("*** DEBUG *** - field = " + field);
            field.setAccessible(true);
            
            MethodHandle getSystemMenuBar = MethodHandles.lookup().unreflectGetter(field);
            System.out.println("*** DEBUG *** - getSystemMenuBar = " + getSystemMenuBar);
            
            MethodHandle setSystemMenuBar = MethodHandles.lookup().unreflectSetter(field);
            System.out.println("*** DEBUG *** - setSystemMenuBar = " + setSystemMenuBar);
            
            Method method = systemMenu.getClass().getDeclaredMethod("insertMenu", Menu.class, MenuBase.class, int.class);
            System.out.println("*** DEBUG *** - method = " + method);
            method.setAccessible(true);
            
            MethodHandle insertMenu = MethodHandles.lookup().unreflect(method);
            System.out.println("*** DEBUG *** - insertMenu = " + insertMenu);

            final Application applicationAdapter = Application.GetApplication();
            System.out.println("*** DEBUG *** - applicationAdapter = " + applicationAdapter);
            if (applicationAdapter == null) {
                return;
            }
            
            final Method hide = applicationAdapter.getClass().getDeclaredMethod("_hide");
            System.out.println("*** DEBUG *** - hide = " + hide);
            hide.setAccessible(true);
            MethodHandles.lookup().unreflect(hide);
            
            final Method hideOtherApplications = applicationAdapter.getClass().getDeclaredMethod("_hideOtherApplications");
            hideOtherApplications.setAccessible(true);
            MethodHandles.lookup().unreflect(hideOtherApplications);

            final Method unhideAllApplications = applicationAdapter.getClass().getDeclaredMethod("_unhideAllApplications");
            unhideAllApplications.setAccessible(true);
            MethodHandles.lookup().unreflect(unhideAllApplications);

            
            // Create the default Application menu
            String appName = "Ancestris";

            MenuItem quitItem = new MenuItem("Quitter " + appName);
            quitItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent e) {
                    Application.EventHandler eh = applicationAdapter.getEventHandler();
                    if (eh != null) {
                        eh.handleQuitAction(Application.GetApplication(), System.nanoTime());
                    }
                }
            });
            quitItem.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.META_DOWN));

            
            MenuItem hideItem = new MenuItem("Cacher " + appName);
            hideItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent e) {
                    Application.EventHandler eh = applicationAdapter.getEventHandler();
                    if (eh != null) {
                        try {
                            hide.invoke(applicationAdapter);
                        } catch (Exception ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            });
            hideItem.setAccelerator(new KeyCodeCombination(KeyCode.H, KeyCombination.META_DOWN));
            
            
            MenuItem hideOthersItem = new MenuItem("Cacher les autres");
            hideOthersItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent e) {
                    Application.EventHandler eh = applicationAdapter.getEventHandler();
                    if (eh != null) {
                        try {
                            hideOtherApplications.invoke(applicationAdapter);
                        } catch (Exception ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            });
            hideOthersItem.setAccelerator(new KeyCodeCombination(KeyCode.H, KeyCombination.META_DOWN, KeyCombination.ALT_DOWN));
            
            MenuItem unhideAllItem = new MenuItem("Tout montrer");
            unhideAllItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent e) {
                    Application.EventHandler eh = applicationAdapter.getEventHandler();
                    if (eh != null) {
                        try {
                            unhideAllApplications.invoke(applicationAdapter);
                        } catch (Exception ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            });
            
            Menu defaultApplicationMenu = new Menu(appName, null);
            defaultApplicationMenu.getItems().add(hideItem);
            defaultApplicationMenu.getItems().add(hideOthersItem);
            defaultApplicationMenu.getItems().add(unhideAllItem);
            defaultApplicationMenu.getItems().add(new SeparatorMenuItem());
            defaultApplicationMenu.getItems().add(quitItem);
        
            // Update the existing Application menu
            System.out.println("*** DEBUG *** - update existing menu");
            MenuBar glassSystemMenuBar = (MenuBar) getSystemMenuBar.invoke(systemMenu);
            System.out.println("*** DEBUG *** - glassSystemMenuBar = " + glassSystemMenuBar);
            if (glassSystemMenuBar == null) {
                setSystemMenuBar.invoke(new Object[] {systemMenu, Application.GetApplication().createMenuBar()});
            } else {
                removeMenu(glassSystemMenuBar, 0);
            }
            System.out.println("*** DEBUG *** - insertMenu menu");
            insertMenu.invoke(new Object[] {systemMenu, null, GlobalMenuAdapter.adapt(defaultApplicationMenu), 0});

            // Since we now have a reference to the menu, we can rename items
            defaultApplicationMenu.getItems().get(1).setText("Hide all the others");
            System.out.println("*** DEBUG *** - END************");
            
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        } catch (Throwable ex) {
            Exceptions.printStackTrace(ex);
        }

        // end

    }

    
    
    
	private void removeMenu(MenuBar glassSystemMenuBar, int index) {
		if (glassSystemMenuBar.getMenus().size() <= index) {
			return;
		}
		clearMenu(glassSystemMenuBar.getMenus().get(index));
		glassSystemMenuBar.remove(index);
	}

	private void clearMenu(com.sun.glass.ui.Menu menu) {
		for (int i = menu.getItems().size() - 1; i >= 0; i--) {
			Object o = menu.getItems().get(i);

			if (o instanceof MenuItem) {
				((com.sun.glass.ui.MenuItem) o).setCallback(null);
			} else if (o instanceof com.sun.glass.ui.MenuItem) {
				clearMenu((com.sun.glass.ui.Menu) o);
			}
		}
		menu.setEventHandler(null);
	}
    
    
}
