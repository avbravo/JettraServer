package io.jettra.flux.annotations;

public class FieldMetadata {
    public String name;
    public Class<?> type;
    public boolean isHidden;
    public boolean isNoEditable;

    public String propertiesLabelValue;
    public String propertiesLabelLabel;

    public boolean hasViewSelectOne;
    public String vsoSource;
    public String vsoMethod;
    public String vsoLabel;
    public String vsoFilter;
    public String vsoFieldOnlyMasterTable;

    public boolean hasViewSelectMany;
    public String vsmSource;
    public String vsmMethod;
    public String vsmLabel;
    public String vsmFilter;
    public String vsmFieldOnlyMasterTable;

    public boolean hasViewDataTable;
    public String vdtRow;
    public String vdtSource;
    public String vdtMethod;
    public String vdtFilter;
    public String vdtEditableRow;
    public boolean vdtShowRowInMasterTable;
    public boolean vdtEditableRowMaster;

    public boolean hasCompute;
    public boolean computeEditable;

    public boolean hasTableColumnField;
    public String tcfField;

    public FieldMetadata(String name, Class<?> type) {
        this.name = name;
        this.type = type;
    }
}
