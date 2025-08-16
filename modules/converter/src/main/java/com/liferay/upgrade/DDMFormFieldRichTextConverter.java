package com.liferay.upgrade;

import com.liferay.dynamic.data.mapping.form.field.type.constants.DDMFormFieldTypeConstants;
import com.liferay.dynamic.data.mapping.model.DDMForm;
import com.liferay.dynamic.data.mapping.model.DDMFormField;
import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.dynamic.data.mapping.service.DDMFieldLocalService;
import com.liferay.dynamic.data.mapping.service.DDMStructureLocalService;
import com.liferay.dynamic.data.mapping.storage.DDMFormFieldValue;
import com.liferay.dynamic.data.mapping.storage.DDMFormValues;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.service.JournalArticleLocalService;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.xml.Element;

import com.liferay.upgrade.compare.DDMFieldAttributesComparator;
import com.liferay.upgrade.helper.DDMFormFieldHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Albert Gomes Cabral
 */
@Component(
    property = {
        "osgi.command.function=convertTextFieldsToRichText",
        "osgi.command.scope=upgrade-utils"
    },
    service = DDMFormFieldRichTextConverter.class
)
public class DDMFormFieldRichTextConverter {

    public void convertTextFieldsToRichText() {
        try {
            // Return every web content, it seems bad, maybe by group is the ideal

            List<JournalArticle> articles = _journalArticleLocalService.getJournalArticles(
                QueryUtil.ALL_POS, QueryUtil.ALL_POS);

            for (JournalArticle article : articles) {
                DDMFormValues ddmFormValues = article.getDDMFormValues();

                DDMForm ddmForm = ddmFormValues.getDDMForm();

                List<DDMFormField> ddmFormFields = ddmForm.getDDMFormFields();

                List<DDMFormField> originalDDMFormFields = DDMFormFieldHelper.clone(
                    ddmFormFields);

                Element rootElement = article.getDocument().getRootElement();

                List<DDMFormField> convertedDDMFormFields = _updateFormFieldsFromElement(
                    ddmFormFields, rootElement);

                if (!DDMFieldAttributesComparator.compare(
                        originalDDMFormFields, convertedDDMFormFields)) {

                    Set<Locale> originalAvailableLocales = ddmFormValues.getAvailableLocales();

                    Locale originalDefaultLocale = ddmFormValues.getDefaultLocale();

                    List<DDMFormFieldValue> originalDDMFormFieldValues =
                        ddmFormValues.getDDMFormFieldValues();

                    ddmForm.setDDMFormFields(convertedDDMFormFields);

                    ddmFormValues = new DDMFormValues(ddmForm);

                    ddmFormValues.setAvailableLocales(originalAvailableLocales);
                    ddmFormValues.setDefaultLocale(originalDefaultLocale);
                    ddmFormValues.setDDMFormFieldValues(originalDDMFormFieldValues);

                    DDMStructure ddmStructure = article.getDDMStructure();

                    _ddmFieldLocalService.updateDDMFormValues(
                        ddmStructure.getStructureId(), article.getId(),
                        ddmFormValues);

                    _updateStructureDefinitionFromFields(
                        convertedDDMFormFields, ddmStructure);

                    StringBundler sb = new StringBundler();

                    sb.append("Updated article").append(System.lineSeparator());
                    sb.append(article.getTitle()).append(System.lineSeparator());
                    sb.append("with id ").append(article.getId());

                    _log.info(sb.toString());
                }

                _log.info("Converter DDM Form fields to RichText finished.");
            }
        }
        catch (Exception exception) {
            _log.error(exception.getMessage(), exception);
        }
    }

    private boolean _containsHtmlContent(Element element) {
        List<Element> dynamicContents = element.elements("dynamic-content");

        for (Element dynamicContentElement : dynamicContents) {
            String data = dynamicContentElement.getText();

            Matcher matcher = Pattern.compile("<[^>]+?/?>").matcher(data);

            if (matcher.find()) {
                return true;
            }
        }

        return false;
    }

    private void _convertNestedTextFieldsToRichText(
        Element parentElement, List<DDMFormField> ddmFormFields) {

        if (parentElement == null) {
            return;
        }

        List<Element> elements = parentElement.elements("dynamic-element");

        for (Element dynamicElementElement : elements) {
            String fieldName = dynamicElementElement.attributeValue("name");

            for (DDMFormField ddmFormField : ddmFormFields) {
                if (!ddmFormField.getName().equals(fieldName)) {
                    continue;
                }

                String type = ddmFormField.getType();

                if (type.equals(DDMFormFieldTypeConstants.FIELDSET)) {
                    _convertNestedTextFieldsToRichText(
                        dynamicElementElement, ddmFormField.getNestedDDMFormFields());
                }
                else if (type.equals(DDMFormFieldTypeConstants.TEXT)) {
                    if (_containsHtmlContent(dynamicElementElement)) {
                        ddmFormField.setType(DDMFormFieldTypeConstants.RICH_TEXT);
                    }
                }
            }
        }
    }

    private List<DDMFormField> _updateFormFieldsFromElement(
        List<DDMFormField> ddmFormFields, Element element) throws PortalException {

        try {
            _convertNestedTextFieldsToRichText(element, ddmFormFields);

            return ddmFormFields;
        }
        catch (RuntimeException runtimeException) {
            throw new PortalException(runtimeException);
        }
    }

    private void _updateStructureDefinitionFromFields(
            List<DDMFormField> ddmFormFields, DDMStructure ddmStructure)
        throws PortalException {

        String definition = ddmStructure.getDefinition();

        JSONObject json = JSONFactoryUtil.createJSONObject(definition);

        JSONArray fields = json.getJSONArray("fields");

        _updateFieldsDefinition(ddmFormFields, fields);

        ddmStructure.setDefinition(json.toString());

        _ddmStructureLocalService.updateDDMStructure(ddmStructure);
    }

    private void _updateFieldsDefinition(
        List<DDMFormField> ddmFormFields, JSONArray fieldsArray) {

        for (int i = 0; i < fieldsArray.length(); i++) {
            JSONObject field = fieldsArray.getJSONObject(i);

            String name = field.getString("name");
            String type = field.getString("type");

            for (DDMFormField ddmFormField : ddmFormFields) {
                if (ddmFormField.getName().equals(name) &&
                        !ddmFormField.getType().equals(type)) {

                    field.put("type", ddmFormField.getType());
                }
            }

            if (field.has("nestedFields")) {
                JSONArray nestedFields = field.getJSONArray("nestedFields");

                if (nestedFields.length() > 0) {
                    DDMFormField nestedDDMFormField = ddmFormFields.stream()
                        .filter(f -> f.getName().equals(name))
                        .findFirst()
                        .orElse(null);

                    List<DDMFormField> nestedDDMFormFields = nestedDDMFormField != null ?
                        nestedDDMFormField.getNestedDDMFormFields() : new ArrayList<>();

                    _updateFieldsDefinition(nestedDDMFormFields, nestedFields);
                }
            }
        }
    }

    @Reference
    private DDMFieldLocalService _ddmFieldLocalService;
    @Reference
    private DDMStructureLocalService _ddmStructureLocalService;
    @Reference
    private JournalArticleLocalService _journalArticleLocalService;

    private static final Log _log = LogFactoryUtil.getLog(DDMFormFieldRichTextConverter.class);

}
