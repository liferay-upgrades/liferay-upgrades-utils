package com.liferay.upgrade.compare;

import com.liferay.dynamic.data.mapping.model.DDMFormField;

import java.util.List;
import java.util.Objects;

/**
 * @author Albert Gomes Cabral
 */
public class DDMFieldAttributesComparator {

    public static boolean compare(List<DDMFormField> list1, List<DDMFormField> list2) {
        if (list1.size() != list2.size()) return false;

        for (int i = 0; i < list1.size(); i++) {
            if (!_deepCompare(list1.get(i), list2.get(i))) {
                return false;
            }
        }

        return true;
    }

    private static boolean _deepCompare(DDMFormField f1, DDMFormField f2) {
        if (f1 == f2) return true;
        if (f1 == null || f2 == null) return false;

        if (!Objects.equals(f1.getName(), f2.getName())) return false;
        if (!Objects.equals(f1.getType(), f2.getType())) return false;

        List<DDMFormField> nested1 = f1.getNestedDDMFormFields();
        List<DDMFormField> nested2 = f2.getNestedDDMFormFields();

        return compare(nested1, nested2);
    }

}
