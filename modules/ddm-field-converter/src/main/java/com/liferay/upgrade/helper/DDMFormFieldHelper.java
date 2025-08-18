package com.liferay.upgrade.helper;

import com.liferay.dynamic.data.mapping.model.DDMFormField;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Albert Gomes Cabral
 */
public class DDMFormFieldHelper {

    public static List<DDMFormField> clone(List<DDMFormField> originalDDMFormFields) {
        List<DDMFormField> ddmFormFields = new ArrayList<>();

        for (DDMFormField originalDDMFormField : originalDDMFormFields) {
            DDMFormField ddmFormField = new DDMFormField();

            ddmFormField.setDataType(originalDDMFormField.getDataType());
            ddmFormField.setDDMFormFieldOptions(originalDDMFormField.getDDMFormFieldOptions());
            ddmFormField.setDDMFormFieldValidation(originalDDMFormField.getDDMFormFieldValidation());
            ddmFormField.setDDMFormLayout(originalDDMFormField.getDDMFormLayout());
            ddmFormField.setFieldNamespace(originalDDMFormField.getFieldNamespace());
            ddmFormField.setFieldReference(originalDDMFormField.getFieldReference());
            ddmFormField.setIndexType(originalDDMFormField.getIndexType());
            ddmFormField.setLabel(originalDDMFormField.getLabel());
            ddmFormField.setLocalizable(originalDDMFormField.isLocalizable());
            ddmFormField.setMultiple(originalDDMFormField.isMultiple());
            ddmFormField.setPredefinedValue(originalDDMFormField.getPredefinedValue());
            ddmFormField.setName(originalDDMFormField.getName());
            ddmFormField.setNestedDDMFormFields(originalDDMFormField.getNestedDDMFormFields());
            ddmFormField.setReadOnly(originalDDMFormField.isReadOnly());
            ddmFormField.setRequired(originalDDMFormField.isRequired());
            ddmFormField.setRequiredErrorMessage(originalDDMFormField.getRequiredErrorMessage());
            ddmFormField.setRepeatable(originalDDMFormField.isRepeatable());
            ddmFormField.setShowLabel(originalDDMFormField.isShowLabel());
            ddmFormField.setStyle(originalDDMFormField.getStyle());
            ddmFormField.setTip(originalDDMFormField.getTip());
            ddmFormField.setVisibilityExpression(originalDDMFormField.getVisibilityExpression());
            ddmFormField.setVisualProperty(originalDDMFormField.isVisualProperty());
            ddmFormField.setType(originalDDMFormField.getType());

            List<DDMFormField> nestedFieldCopy = clone(originalDDMFormField.getNestedDDMFormFields());
            ddmFormField.setNestedDDMFormFields(nestedFieldCopy);

            ddmFormFields.add(ddmFormField);
        }

        return ddmFormFields;
    }

}
