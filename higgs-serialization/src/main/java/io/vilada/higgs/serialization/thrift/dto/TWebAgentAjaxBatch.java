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
@javax.annotation.Generated(value = "Autogenerated by Thrift Compiler (0.10.0)", date = "2017-10-16")
public class TWebAgentAjaxBatch implements org.apache.thrift.TBase<TWebAgentAjaxBatch, TWebAgentAjaxBatch._Fields>, java.io.Serializable, Cloneable, Comparable<TWebAgentAjaxBatch> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("TWebAgentAjaxBatch");

  private static final org.apache.thrift.protocol.TField AJAX_BATCH_FIELD_DESC = new org.apache.thrift.protocol.TField("ajaxBatch", org.apache.thrift.protocol.TType.LIST, (short)1);

  private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new TWebAgentAjaxBatchStandardSchemeFactory();
  private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new TWebAgentAjaxBatchTupleSchemeFactory();

  public java.util.List<TWebAgentAjax> ajaxBatch; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    AJAX_BATCH((short)1, "ajaxBatch");

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
        case 1: // AJAX_BATCH
          return AJAX_BATCH;
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
  public static final java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.AJAX_BATCH, new org.apache.thrift.meta_data.FieldMetaData("ajaxBatch", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRUCT            , "TWebAgentAjax"))));
    metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(TWebAgentAjaxBatch.class, metaDataMap);
  }

  public TWebAgentAjaxBatch() {
  }

  public TWebAgentAjaxBatch(
    java.util.List<TWebAgentAjax> ajaxBatch)
  {
    this();
    this.ajaxBatch = ajaxBatch;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public TWebAgentAjaxBatch(TWebAgentAjaxBatch other) {
    if (other.isSetAjaxBatch()) {
      java.util.List<TWebAgentAjax> __this__ajaxBatch = new java.util.ArrayList<TWebAgentAjax>(other.ajaxBatch.size());
      for (TWebAgentAjax other_element : other.ajaxBatch) {
        __this__ajaxBatch.add(other_element);
      }
      this.ajaxBatch = __this__ajaxBatch;
    }
  }

  public TWebAgentAjaxBatch deepCopy() {
    return new TWebAgentAjaxBatch(this);
  }


  public void clear() {
    this.ajaxBatch = null;
  }

  public int getAjaxBatchSize() {
    return (this.ajaxBatch == null) ? 0 : this.ajaxBatch.size();
  }

  public java.util.Iterator<TWebAgentAjax> getAjaxBatchIterator() {
    return (this.ajaxBatch == null) ? null : this.ajaxBatch.iterator();
  }

  public void addToAjaxBatch(TWebAgentAjax elem) {
    if (this.ajaxBatch == null) {
      this.ajaxBatch = new java.util.ArrayList<TWebAgentAjax>();
    }
    this.ajaxBatch.add(elem);
  }

  public java.util.List<TWebAgentAjax> getAjaxBatch() {
    return this.ajaxBatch;
  }

  public TWebAgentAjaxBatch setAjaxBatch(java.util.List<TWebAgentAjax> ajaxBatch) {
    this.ajaxBatch = ajaxBatch;
    return this;
  }

  public void unsetAjaxBatch() {
    this.ajaxBatch = null;
  }

  /** Returns true if field ajaxBatch is set (has been assigned a value) and false otherwise */
  public boolean isSetAjaxBatch() {
    return this.ajaxBatch != null;
  }

  public void setAjaxBatchIsSet(boolean value) {
    if (!value) {
      this.ajaxBatch = null;
    }
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case AJAX_BATCH:
      if (value == null) {
        unsetAjaxBatch();
      } else {
        setAjaxBatch((java.util.List<TWebAgentAjax>)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case AJAX_BATCH:
      return getAjaxBatch();

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case AJAX_BATCH:
      return isSetAjaxBatch();
    }
    throw new IllegalStateException();
  }


  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof TWebAgentAjaxBatch)
      return this.equals((TWebAgentAjaxBatch)that);
    return false;
  }

  public boolean equals(TWebAgentAjaxBatch that) {
    if (that == null)
      return false;
    if (this == that)
      return true;

    boolean this_present_ajaxBatch = true && this.isSetAjaxBatch();
    boolean that_present_ajaxBatch = true && that.isSetAjaxBatch();
    if (this_present_ajaxBatch || that_present_ajaxBatch) {
      if (!(this_present_ajaxBatch && that_present_ajaxBatch))
        return false;
      if (!this.ajaxBatch.equals(that.ajaxBatch))
        return false;
    }

    return true;
  }


  public int hashCode() {
    int hashCode = 1;

    hashCode = hashCode * 8191 + ((isSetAjaxBatch()) ? 131071 : 524287);
    if (isSetAjaxBatch())
      hashCode = hashCode * 8191 + ajaxBatch.hashCode();

    return hashCode;
  }


  public int compareTo(TWebAgentAjaxBatch other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = Boolean.valueOf(isSetAjaxBatch()).compareTo(other.isSetAjaxBatch());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetAjaxBatch()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.ajaxBatch, other.ajaxBatch);
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
    StringBuilder sb = new StringBuilder("TWebAgentAjaxBatch(");
    boolean first = true;

    sb.append("ajaxBatch:");
    if (this.ajaxBatch == null) {
      sb.append("null");
    } else {
      sb.append(this.ajaxBatch);
    }
    first = false;
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

  private static class TWebAgentAjaxBatchStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public TWebAgentAjaxBatchStandardScheme getScheme() {
      return new TWebAgentAjaxBatchStandardScheme();
    }
  }

  private static class TWebAgentAjaxBatchStandardScheme extends org.apache.thrift.scheme.StandardScheme<TWebAgentAjaxBatch> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, TWebAgentAjaxBatch struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // AJAX_BATCH
            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
              {
                org.apache.thrift.protocol.TList _list28 = iprot.readListBegin();
                struct.ajaxBatch = new java.util.ArrayList<TWebAgentAjax>(_list28.size);
                TWebAgentAjax _elem29;
                for (int _i30 = 0; _i30 < _list28.size; ++_i30)
                {
                  _elem29 = new TWebAgentAjax();
                  _elem29.read(iprot);
                  struct.ajaxBatch.add(_elem29);
                }
                iprot.readListEnd();
              }
              struct.setAjaxBatchIsSet(true);
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

    public void write(org.apache.thrift.protocol.TProtocol oprot, TWebAgentAjaxBatch struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.ajaxBatch != null) {
        oprot.writeFieldBegin(AJAX_BATCH_FIELD_DESC);
        {
          oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, struct.ajaxBatch.size()));
          for (TWebAgentAjax _iter31 : struct.ajaxBatch)
          {
            _iter31.write(oprot);
          }
          oprot.writeListEnd();
        }
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class TWebAgentAjaxBatchTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public TWebAgentAjaxBatchTupleScheme getScheme() {
      return new TWebAgentAjaxBatchTupleScheme();
    }
  }

  private static class TWebAgentAjaxBatchTupleScheme extends org.apache.thrift.scheme.TupleScheme<TWebAgentAjaxBatch> {


    public void write(org.apache.thrift.protocol.TProtocol prot, TWebAgentAjaxBatch struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      java.util.BitSet optionals = new java.util.BitSet();
      if (struct.isSetAjaxBatch()) {
        optionals.set(0);
      }
      oprot.writeBitSet(optionals, 1);
      if (struct.isSetAjaxBatch()) {
        {
          oprot.writeI32(struct.ajaxBatch.size());
          for (TWebAgentAjax _iter32 : struct.ajaxBatch)
          {
            _iter32.write(oprot);
          }
        }
      }
    }


    public void read(org.apache.thrift.protocol.TProtocol prot, TWebAgentAjaxBatch struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      java.util.BitSet incoming = iprot.readBitSet(1);
      if (incoming.get(0)) {
        {
          org.apache.thrift.protocol.TList _list33 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
          struct.ajaxBatch = new java.util.ArrayList<TWebAgentAjax>(_list33.size);
          TWebAgentAjax _elem34;
          for (int _i35 = 0; _i35 < _list33.size; ++_i35)
          {
            _elem34 = new TWebAgentAjax();
            _elem34.read(iprot);
            struct.ajaxBatch.add(_elem34);
          }
        }
        struct.setAjaxBatchIsSet(true);
      }
    }
  }

  private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
    return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
  }
}

