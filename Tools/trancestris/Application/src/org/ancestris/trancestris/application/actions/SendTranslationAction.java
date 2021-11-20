/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ancestris.trancestris.application.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.prefs.Preferences;
import org.ancestris.trancestris.explorers.zipexplorer.ZipExplorerTopComponent;
import org.ancestris.trancestris.resources.ZipArchive;
import org.netbeans.api.options.OptionsDisplayer;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.ancestris.trancestris.application.utils.SendMessageWorker;

public final class SendTranslationAction implements ActionListener {

    private final static String SEND = NbBundle.getMessage(SendTranslationAction.class, "SendTranslationAction.button.send");
    private Preferences modulePreferences = NbPreferences.forModule(SendTranslationAction.class);
    File zipOutputFile = null;
    SendTranslationPanel sendTranslationPanel = new SendTranslationPanel();
    String archiveName = "";
    String filePath = "";
    String prefix = "";
    String suffix = "";
    String toLocale = "";
    String fromLocale = "";
    
    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        if (modulePreferences.get("mail.host", "").equals("")) {
            NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(SendTranslationPanel.class, "SendTranslationPanel.msg.setParameters"), NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
            OptionsDisplayer.getDefault().open("SendTranslation");
        } else {
            TopComponent tc = WindowManager.getDefault().findTopComponent("ZipExplorerTopComponent");

            ZipArchive zipArchive = ((ZipExplorerTopComponent) tc).getBundles();
            if (zipArchive != null) {
                zipArchive.write();
                boolean confirmed = false, done = false;
                while (!done) {
                    if (zipArchive.hasTranslation() == true || confirmed) {
                        archiveName = zipArchive.getName();
                        filePath = zipArchive.getZipFile().getParent();
                        prefix = archiveName.substring(0, archiveName.indexOf('.'));
                        suffix = archiveName.substring(archiveName.indexOf('.') + 1);
                        toLocale = zipArchive.getToLocale().getLanguage();
                        fromLocale = zipArchive.getFromLocale().getLanguage();

                        setDefaultValues(sendTranslationPanel);
                        DialogDescriptor dd = new DialogDescriptor(sendTranslationPanel, NbBundle.getMessage(this.getClass(), "SendTranslationPanel.title"));
                        dd.setOptions(new Object[]{SEND, DialogDescriptor.CANCEL_OPTION});
                        DialogDisplayer.getDefault().createDialog(dd);
                        DialogDisplayer.getDefault().notify(dd);
                        if (dd.getValue().equals(SEND)) {
                            zipOutputFile = new File(filePath + File.separator + prefix + "_" + toLocale + "." + suffix);
                            if (!zipOutputFile.exists()) {
                                try {
                                    zipOutputFile.createNewFile();
                                } catch (IOException ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                            }

                            int nbTranslatedFiles = zipArchive.saveTranslation(zipOutputFile);

                            saveValues(sendTranslationPanel);
                            String subject = sendTranslationPanel.getSubjectFormattedTextField();
                            String name = sendTranslationPanel.getNameFormattedTextField();
                            String from = sendTranslationPanel.getEmailFormattedTextField();
                            String message = sendTranslationPanel.getMessageTextArea();
                            String to = sendTranslationPanel.getMailToFormattedTextField();
                            if (nbTranslatedFiles > 1) {
                                message += "\n \n ----- Do not delete the following sentence ----- \n       (.) There are " + nbTranslatedFiles + " files in the bundle \n ------------==:==:==:==:==:==:==:==------------ \n ";
                            } else {
                                message += "\n \n ----- Do not delete the following sentence ----- \n       (.) There is one file in the bundle \n ------------==:==:==:==:==:==:==:==------------ \n ";
                            }
                            Thread t = new Thread(new SendMessageWorker(name, from, to, subject, message, zipOutputFile, zipArchive));
                            t.start();
                        }
                        done = true;
                    } else {
                        NotifyDescriptor nd = new NotifyDescriptor.Confirmation(NbBundle.getMessage(SendTranslationAction.class, "SendTranslationAction.msg.nothingToSend"), NotifyDescriptor.QUESTION_MESSAGE, NotifyDescriptor.YES_NO_OPTION);
                        if (DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.OK_OPTION) {
                            confirmed = true;
                        } else {
                            done = true;
                        }
                    }
                }
            }
        }
    }

    private void setDefaultValues(SendTranslationPanel sendTranslationPanel) {
//        sendTranslationPanel.setMailToFormattedTextField(modulePreferences.get("mailto.address", "frederic@ancestris.org"));
        sendTranslationPanel.setMailToFormattedTextField("frederic@ancestris.org");
        sendTranslationPanel.setNameFormattedTextField(modulePreferences.get("mail.name", ""));
        sendTranslationPanel.setEmailFormattedTextField(modulePreferences.get("mail.address", ""));
        String TS = new SimpleDateFormat(NbBundle.getMessage(SendTranslationAction.class, "SendTranslationAction.msg.subject.date")).format(new Date());
        String subject = "[" + NbBundle.getMessage(SendTranslationAction.class, "SendTranslationAction.msg.subject.tag", fromLocale, toLocale) + " " + TS + "] ";
        sendTranslationPanel.setSubjectFormattedTextField(subject);
    }

    private void saveValues(SendTranslationPanel sendTranslationPanel) {
        modulePreferences.put("mail.name", sendTranslationPanel.getNameFormattedTextField());
        modulePreferences.put("mail.address", sendTranslationPanel.getEmailFormattedTextField());
        modulePreferences.put("mailto.address", sendTranslationPanel.getMailToFormattedTextField());
    }
}
