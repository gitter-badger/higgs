/*
 * Copyright 2018 The Higgs Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Autogenerated by Thrift Compiler (0.10.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package io.vilada.higgs.serialization.thrift.dto;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked", "unused"})
@javax.annotation.Generated(value = "Autogenerated by Thrift Compiler (0.10.0)", date = "2017-08-02")
public class TAgentResult implements org.apache.thrift.TBase<TAgentResult, TAgentResult._Fields>, java.io.Serializable, Cloneable, Comparable<TAgentResult> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("TAgentResult");

  private static final org.apache.thrift.protocol.TField DATA_FIELD_DESC = new org.apache.thrift.protocol.TField("data", org.apache.thrift.protocol.TType.STRING, (short)1);
  private static final org.apache.thrift.protocol.TField EXTRA_DATA_TYPE_FIELD_DESC = new org.apache.thrift.protocol.TField("extraDataType", org.apache.thrift.protocol.TType.STRING, (short)2);
  private static final org.apache.thrift.protocol.TField EXTRA_DATA_FIELD_DESC = new org.apache.thrift.protocol.TField("extraData", org.apache.thrift.protocol.TType.STRING, (short)3);

  private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new TAgentResultStandardSchemeFactory();
  private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new TAgentResultTupleSchemeFactory();

  public java.nio.ByteBuffer data; // required
  public String extraDataType; // optional
  public java.nio.ByteBuffer extraData; // optional

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    DATA((short)1, "data"),
    EXTRA_DATA_TYPE((short)2, "extraDataType"),
    EXTRA_DATA((short)3, "extraData");

    private static final java.util.Map<String, _Fields> byName = new java.util.HashMap<String, _Fields>();

    static {
      for (_Fields field : java.util.EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // DATA
          return DATA;
        case 2: // EXTRA_DATA_TYPE
          return EXTRA_DATA_TYPE;
        case 3: // EXTRA_DATA
          return EXTRA_DATA;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    public static _Fields findByName(String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final String _fieldName;

    _Fields(short thriftId, String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments
  private static final _Fields optionals[] = {_Fields.EXTRA_DATA_TYPE,_Fields.EXTRA_DATA};
  public static final java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.DATA, new org.apache.thrift.meta_data.FieldMetaData("data", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING        , true)));
    tmpMap.put(_Fields.EXTRA_DATA_TYPE, new org.apache.thrift.meta_data.FieldMetaData("extraDataType", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.EXTRA_DATA, new org.apache.thrift.meta_data.FieldMetaData("extraData", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING        , true)));
    metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(TAgentResult.class, metaDataMap);
  }

  public TAgentResult() {
  }

  public TAgentResult(
    java.nio.ByteBuffer data)
  {
    this();
    this.data = org.apache.thrift.TBaseHelper.copyBinary(data);
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public TAgentResult(TAgentResult other) {
    if (other.isSetData()) {
      this.data = org.apache.thrift.TBaseHelper.copyBinary(other.data);
    }
    if (other.isSetExtraDataType()) {
      this.extraDataType = other.extraDataType;
    }
    if (other.isSetExtraData()) {
      this.extraData = org.apache.thrift.TBaseHelper.copyBinary(other.extraData);
    }
  }

  public TAgentResult deepCopy() {
    return new TAgentResult(this);
  }


  public void clear() {
    this.data = null;
    this.extraDataType = null;
    this.extraData = null;
  }

  public byte[] getData() {
    setData(org.apache.thrift.TBaseHelper.rightSize(data));
    return data == null ? null : data.array();
  }

  public java.nio.ByteBuffer bufferForData() {
    return org.apache.thrift.TBaseHelper.copyBinary(data);
  }

  public TAgentResult setData(byte[] data) {
    this.data = data == null ? (java.nio.ByteBuffer)null : java.nio.ByteBuffer.wrap(data.clone());
    return this;
  }

  public TAgentResult setData(java.nio.ByteBuffer data) {
    this.data = org.apache.thrift.TBaseHelper.copyBinary(data);
    return this;
  }

  public void unsetData() {
    this.data = null;
  }

  /** Returns true if field data is set (has been assigned a value) and false otherwise */
  public boolean isSetData() {
    return this.data != null;
  }

  public void setDataIsSet(boolean value) {
    if (!value) {
      this.data = null;
    }
  }

  public String getExtraDataType() {
    return this.extraDataType;
  }

  public TAgentResult setExtraDataType(String extraDataType) {
    this.extraDataType = extraDataType;
    return this;
  }

  public void unsetExtraDataType() {
    this.extraDataType = null;
  }

  /** Returns true if field extraDataType is set (has been assigned a value) and false otherwise */
  public boolean isSetExtraDataType() {
    return this.extraDataType != null;
  }

  public void setExtraDataTypeIsSet(boolean value) {
    if (!value) {
      this.extraDataType = null;
    }
  }

  public byte[] getExtraData() {
    setExtraData(org.apache.thrift.TBaseHelper.rightSize(extraData));
    return extraData == null ? null : extraData.array();
  }

  public java.nio.ByteBuffer bufferForExtraData() {
    return org.apache.thrift.TBaseHelper.copyBinary(extraData);
  }

  public TAgentResult setExtraData(byte[] extraData) {
    this.extraData = extraData == null ? (java.nio.ByteBuffer)null : java.nio.ByteBuffer.wrap(extraData.clone());
    return this;
  }

  public TAgentResult setExtraData(java.nio.ByteBuffer extraData) {
    this.extraData = org.apache.thrift.TBaseHelper.copyBinary(extraData);
    return this;
  }

  public void unsetExtraData() {
    this.extraData = null;
  }

  /** Returns true if field extraData is set (has been assigned a value) and false otherwise */
  public boolean isSetExtraData() {
    return this.extraData != null;
  }

  public void setExtraDataIsSet(boolean value) {
    if (!value) {
      this.extraData = null;
    }
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case DATA:
      if (value == null) {
        unsetData();
      } else {
        if (value instanceof byte[]) {
          setData((byte[])value);
        } else {
          setData((java.nio.ByteBuffer)value);
        }
      }
      break;

    case EXTRA_DATA_TYPE:
      if (value == null) {
        unsetExtraDataType();
      } else {
        setExtraDataType((String)value);
      }
      break;

    case EXTRA_DATA:
      if (value == null) {
        unsetExtraData();
      } else {
        if (value instanceof byte[]) {
          setExtraData((byte[])value);
        } else {
          setExtraData((java.nio.ByteBuffer)value);
        }
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case DATA:
      return getData();

    case EXTRA_DATA_TYPE:
      return getExtraDataType();

    case EXTRA_DATA:
      return getExtraData();

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case DATA:
      return isSetData();
    case EXTRA_DATA_TYPE:
      return isSetExtraDataType();
    case EXTRA_DATA:
      return isSetExtraData();
    }
    throw new IllegalStateException();
  }


  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof TAgentResult)
      return this.equals((TAgentResult)that);
    return false;
  }

  public boolean equals(TAgentResult that) {
    if (that == null)
      return false;
    if (this == that)
      return true;

    boolean this_present_data = true && this.isSetData();
    boolean that_present_data = true && that.isSetData();
    if (this_present_data || that_present_data) {
      if (!(this_present_data && that_present_data))
        return false;
      if (!this.data.equals(that.data))
        return false;
    }

    boolean this_present_extraDataType = true && this.isSetExtraDataType();
    boolean that_present_extraDataType = true && that.isSetExtraDataType();
    if (this_present_extraDataType || that_present_extraDataType) {
      if (!(this_present_extraDataType && that_present_extraDataType))
        return false;
      if (!this.extraDataType.equals(that.extraDataType))
        return false;
    }

    boolean this_present_extraData = true && this.isSetExtraData();
    boolean that_present_extraData = true && that.isSetExtraData();
    if (this_present_extraData || that_present_extraData) {
      if (!(this_present_extraData && that_present_extraData))
        return false;
      if (!this.extraData.equals(that.extraData))
        return false;
    }

    return true;
  }


  public int hashCode() {
    int hashCode = 1;

    hashCode = hashCode * 8191 + ((isSetData()) ? 131071 : 524287);
    if (isSetData())
      hashCode = hashCode * 8191 + data.hashCode();

    hashCode = hashCode * 8191 + ((isSetExtraDataType()) ? 131071 : 524287);
    if (isSetExtraDataType())
      hashCode = hashCode * 8191 + extraDataType.hashCode();

    hashCode = hashCode * 8191 + ((isSetExtraData()) ? 131071 : 524287);
    if (isSetExtraData())
      hashCode = hashCode * 8191 + extraData.hashCode();

    return hashCode;
  }


  public int compareTo(TAgentResult other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = Boolean.valueOf(isSetData()).compareTo(other.isSetData());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetData()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.data, other.data);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetExtraDataType()).compareTo(other.isSetExtraDataType());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetExtraDataType()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.extraDataType, other.extraDataType);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetExtraData()).compareTo(other.isSetExtraData());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetExtraData()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.extraData, other.extraData);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
    scheme(iprot).read(iprot, this);
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    scheme(oprot).write(oprot, this);
  }


  public String toString() {
    StringBuilder sb = new StringBuilder("TAgentResult(");
    boolean first = true;

    sb.append("data:");
    if (this.data == null) {
      sb.append("null");
    } else {
      org.apache.thrift.TBaseHelper.toString(this.data, sb);
    }
    first = false;
    if (isSetExtraDataType()) {
      if (!first) sb.append(", ");
      sb.append("extraDataType:");
      if (this.extraDataType == null) {
        sb.append("null");
      } else {
        sb.append(this.extraDataType);
      }
      first = false;
    }
    if (isSetExtraData()) {
      if (!first) sb.append(", ");
      sb.append("extraData:");
      if (this.extraData == null) {
        sb.append("null");
      } else {
        org.apache.thrift.TBaseHelper.toString(this.extraData, sb);
      }
      first = false;
    }
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    // check for sub-struct validity
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    try {
      write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
    try {
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class TAgentResultStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public TAgentResultStandardScheme getScheme() {
      return new TAgentResultStandardScheme();
    }
  }

  private static class TAgentResultStandardScheme extends org.apache.thrift.scheme.StandardScheme<TAgentResult> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, TAgentResult struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // DATA
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.data = iprot.readBinary();
              struct.setDataIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // EXTRA_DATA_TYPE
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.extraDataType = iprot.readString();
              struct.setExtraDataTypeIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // EXTRA_DATA
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.extraData = iprot.readBinary();
              struct.setExtraDataIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          default:
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();

      // check for required fields of primitive type, which can't be checked in the validate method
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, TAgentResult struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.data != null) {
        oprot.writeFieldBegin(DATA_FIELD_DESC);
        oprot.writeBinary(struct.data);
        oprot.writeFieldEnd();
      }
      if (struct.extraDataType != null) {
        if (struct.isSetExtraDataType()) {
          oprot.writeFieldBegin(EXTRA_DATA_TYPE_FIELD_DESC);
          oprot.writeString(struct.extraDataType);
          oprot.writeFieldEnd();
        }
      }
      if (struct.extraData != null) {
        if (struct.isSetExtraData()) {
          oprot.writeFieldBegin(EXTRA_DATA_FIELD_DESC);
          oprot.writeBinary(struct.extraData);
          oprot.writeFieldEnd();
        }
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class TAgentResultTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public TAgentResultTupleScheme getScheme() {
      return new TAgentResultTupleScheme();
    }
  }

  private static class TAgentResultTupleScheme extends org.apache.thrift.scheme.TupleScheme<TAgentResult> {


    public void write(org.apache.thrift.protocol.TProtocol prot, TAgentResult struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      java.util.BitSet optionals = new java.util.BitSet();
      if (struct.isSetData()) {
        optionals.set(0);
      }
      if (struct.isSetExtraDataType()) {
        optionals.set(1);
      }
      if (struct.isSetExtraData()) {
        optionals.set(2);
      }
      oprot.writeBitSet(optionals, 3);
      if (struct.isSetData()) {
        oprot.writeBinary(struct.data);
      }
      if (struct.isSetExtraDataType()) {
        oprot.writeString(struct.extraDataType);
      }
      if (struct.isSetExtraData()) {
        oprot.writeBinary(struct.extraData);
      }
    }


    public void read(org.apache.thrift.protocol.TProtocol prot, TAgentResult struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      java.util.BitSet incoming = iprot.readBitSet(3);
      if (incoming.get(0)) {
        struct.data = iprot.readBinary();
        struct.setDataIsSet(true);
      }
      if (incoming.get(1)) {
        struct.extraDataType = iprot.readString();
        struct.setExtraDataTypeIsSet(true);
      }
      if (incoming.get(2)) {
        struct.extraData = iprot.readBinary();
        struct.setExtraDataIsSet(true);
      }
    }
  }

  private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
    return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
  }
}
