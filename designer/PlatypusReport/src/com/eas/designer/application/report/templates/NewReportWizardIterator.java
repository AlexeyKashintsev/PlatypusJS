/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eas.designer.application.report.templates;

import com.eas.designer.explorer.files.wizard.NewApplicationElementWizardIterator;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.api.templates.TemplateRegistrations;

/**
 *
 * @author mg
 */
public class NewReportWizardIterator extends NewApplicationElementWizardIterator {

    public NewReportWizardIterator() {
        super();
    }

    @TemplateRegistrations({
    @TemplateRegistration(
            folder = "Platypus application elements",
    position = 300,
    content = {"PlatypusReportTemplate.js", "PlatypusReportTemplate.model", "PlatypusReportTemplate.xlsx"},
    displayName = "#Templates/Other/PlatypusReportTemplate",
    description = "Report.html",
    scriptEngine = "freemarker"),
    @TemplateRegistration(
            folder = "Platypus application elements/Resources",
    position = 310,
    content = {"PlatypusReportTemplateTemplate.xlsx"},
    displayName = "#Templates/Other/PlatypusReportTemplateTemplate",
    description = "ReportTemplate.html")})
    public static NewApplicationElementWizardIterator createIterator() {
        return new NewReportWizardIterator();
    }
}
