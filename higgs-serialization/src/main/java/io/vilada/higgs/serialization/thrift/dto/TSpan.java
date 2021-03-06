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
@javax.annotation.Generated(value = "Autogenerated by Thrift Compiler (0.10.0)", date = "2018-01-22")
public class TSpan implements org.apache.thrift.TBase<TSpan, TSpan._Fields>, java.io.Serializable, Cloneable, Comparable<TSpan> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("TSpan");

  private static final org.apache.thrift.protocol.TField OPERATION_NAME_FIELD_DESC = new org.apache.thrift.protocol.TField("operationName", org.apache.thrift.protocol.TType.STRING, (short)1);
  private static final org.apache.thrift.protocol.TField START_TIME_FIELD_DESC = new org.apache.thrift.protocol.TField("startTime", org.apache.thrift.protocol.TType.I64, (short)2);
  private static final org.apache.thrift.protocol.TField FINISH_TIME_FIELD_DESC = new org.apache.thrift.protocol.TField("finishTime", org.apache.thrift.protocol.TType.I64, (short)3);
  private static final org.apache.thrift.protocol.TField SPAN_TAGS_FIELD_DESC = new org.apache.thrift.protocol.TField("spanTags", org.apache.thrift.protocol.TType.MAP, (short)4);
  private static final org.apache.thrift.protocol.TField SPAN_LOGS_FIELD_DESC = new org.apache.thrift.protocol.TField("spanLogs", org.apache.thrift.protocol.TType.LIST, (short)5);
  private static final org.apache.thrift.protocol.TField CONTEXT_FIELD_DESC = new org.apache.thrift.protocol.TField("context", org.apache.thrift.protocol.TType.STRUCT, (short)6);

  private static final org.apache.thrift.scheme.SchemeFactory STANDARD_SCHEME_FACTORY = new TSpanStandardSchemeFactory();
  private static final org.apache.thrift.scheme.SchemeFactory TUPLE_SCHEME_FACTORY = new TSpanTupleSchemeFactory();

  public String operationName; // required
  public long startTime; // required
  public long finishTime; // required
  public java.util.Map<String,String> spanTags; // optional
  public java.util.List<TSpanLog> spanLogs; // optional
  public TSpanContext context; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    OPERATION_NAME((short)1, "operationName"),
    START_TIME((short)2, "startTime"),
    FINISH_TIME((short)3, "finishTime"),
    SPAN_TAGS((short)4, "spanTags"),
    SPAN_LOGS((short)5, "spanLogs"),
    CONTEXT((short)6, "context");

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
        case 1: // OPERATION_NAME
          return OPERATION_NAME;
        case 2: // START_TIME
          return START_TIME;
        case 3: // FINISH_TIME
          return FINISH_TIME;
        case 4: // SPAN_TAGS
          return SPAN_TAGS;
        case 5: // SPAN_LOGS
          return SPAN_LOGS;
        case 6: // CONTEXT
          return CONTEXT;
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
  private static final int __STARTTIME_ISSET_ID = 0;
  private static final int __FINISHTIME_ISSET_ID = 1;
  private byte __isset_bitfield = 0;
  private static final _Fields optionals[] = {_Fields.SPAN_TAGS,_Fields.SPAN_LOGS};
  public static final java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    java.util.Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new java.util.EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.OPERATION_NAME, new org.apache.thrift.meta_data.FieldMetaData("operationName", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.START_TIME, new org.apache.thrift.meta_data.FieldMetaData("startTime", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
    tmpMap.put(_Fields.FINISH_TIME, new org.apache.thrift.meta_data.FieldMetaData("finishTime", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I64)));
    tmpMap.put(_Fields.SPAN_TAGS, new org.apache.thrift.meta_data.FieldMetaData("spanTags", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.MapMetaData(org.apache.thrift.protocol.TType.MAP, 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING), 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING))));
    tmpMap.put(_Fields.SPAN_LOGS, new org.apache.thrift.meta_data.FieldMetaData("spanLogs", org.apache.thrift.TFieldRequirementType.OPTIONAL, 
        new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
            new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRUCT            , "TSpanLog"))));
    tmpMap.put(_Fields.CONTEXT, new org.apache.thrift.meta_data.FieldMetaData("context", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRUCT        , "TSpanContext")));
    metaDataMap = java.util.Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(TSpan.class, metaDataMap);
  }

  public TSpan() {
  }

  public TSpan(
    String operationName,
    long startTime,
    long finishTime,
    TSpanContext context)
  {
    this();
    this.operationName = operationName;
    this.startTime = startTime;
    setStartTimeIsSet(true);
    this.finishTime = finishTime;
    setFinishTimeIsSet(true);
    this.context = context;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public TSpan(TSpan other) {
    __isset_bitfield = other.__isset_bitfield;
    if (other.isSetOperationName()) {
      this.operationName = other.operationName;
    }
    this.startTime = other.startTime;
    this.finishTime = other.finishTime;
    if (other.isSetSpanTags()) {
      java.util.Map<String,String> __this__spanTags = new java.util.HashMap<String,String>(other.spanTags);
      this.spanTags = __this__spanTags;
    }
    if (other.isSetSpanLogs()) {
      java.util.List<TSpanLog> __this__spanLogs = new java.util.ArrayList<TSpanLog>(other.spanLogs.size());
      for (TSpanLog other_element : other.spanLogs) {
        __this__spanLogs.add(other_element);
      }
      this.spanLogs = __this__spanLogs;
    }
    if (other.isSetContext()) {
      this.context = new TSpanContext(other.context);
    }
  }

  public TSpan deepCopy() {
    return new TSpan(this);
  }

  public void clear() {
    this.operationName = null;
    setStartTimeIsSet(false);
    this.startTime = 0;
    setFinishTimeIsSet(false);
    this.finishTime = 0;
    this.spanTags = null;
    this.spanLogs = null;
    this.context = null;
  }

  public String getOperationName() {
    return this.operationName;
  }

  public TSpan setOperationName(String operationName) {
    this.operationName = operationName;
    return this;
  }

  public void unsetOperationName() {
    this.operationName = null;
  }

  /** Returns true if field operationName is set (has been assigned a value) and false otherwise */
  public boolean isSetOperationName() {
    return this.operationName != null;
  }

  public void setOperationNameIsSet(boolean value) {
    if (!value) {
      this.operationName = null;
    }
  }

  public long getStartTime() {
    return this.startTime;
  }

  public TSpan setStartTime(long startTime) {
    this.startTime = startTime;
    setStartTimeIsSet(true);
    return this;
  }

  public void unsetStartTime() {
    __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __STARTTIME_ISSET_ID);
  }

  /** Returns true if field startTime is set (has been assigned a value) and false otherwise */
  public boolean isSetStartTime() {
    return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __STARTTIME_ISSET_ID);
  }

  public void setStartTimeIsSet(boolean value) {
    __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __STARTTIME_ISSET_ID, value);
  }

  public long getFinishTime() {
    return this.finishTime;
  }

  public TSpan setFinishTime(long finishTime) {
    this.finishTime = finishTime;
    setFinishTimeIsSet(true);
    return this;
  }

  public void unsetFinishTime() {
    __isset_bitfield = org.apache.thrift.EncodingUtils.clearBit(__isset_bitfield, __FINISHTIME_ISSET_ID);
  }

  /** Returns true if field finishTime is set (has been assigned a value) and false otherwise */
  public boolean isSetFinishTime() {
    return org.apache.thrift.EncodingUtils.testBit(__isset_bitfield, __FINISHTIME_ISSET_ID);
  }

  public void setFinishTimeIsSet(boolean value) {
    __isset_bitfield = org.apache.thrift.EncodingUtils.setBit(__isset_bitfield, __FINISHTIME_ISSET_ID, value);
  }

  public int getSpanTagsSize() {
    return (this.spanTags == null) ? 0 : this.spanTags.size();
  }

  public void putToSpanTags(String key, String val) {
    if (this.spanTags == null) {
      this.spanTags = new java.util.HashMap<String,String>();
    }
    this.spanTags.put(key, val);
  }

  public java.util.Map<String,String> getSpanTags() {
    return this.spanTags;
  }

  public TSpan setSpanTags(java.util.Map<String,String> spanTags) {
    this.spanTags = spanTags;
    return this;
  }

  public void unsetSpanTags() {
    this.spanTags = null;
  }

  /** Returns true if field spanTags is set (has been assigned a value) and false otherwise */
  public boolean isSetSpanTags() {
    return this.spanTags != null;
  }

  public void setSpanTagsIsSet(boolean value) {
    if (!value) {
      this.spanTags = null;
    }
  }

  public int getSpanLogsSize() {
    return (this.spanLogs == null) ? 0 : this.spanLogs.size();
  }

  public java.util.Iterator<TSpanLog> getSpanLogsIterator() {
    return (this.spanLogs == null) ? null : this.spanLogs.iterator();
  }

  public void addToSpanLogs(TSpanLog elem) {
    if (this.spanLogs == null) {
      this.spanLogs = new java.util.ArrayList<TSpanLog>();
    }
    this.spanLogs.add(elem);
  }

  public java.util.List<TSpanLog> getSpanLogs() {
    return this.spanLogs;
  }

  public TSpan setSpanLogs(java.util.List<TSpanLog> spanLogs) {
    this.spanLogs = spanLogs;
    return this;
  }

  public void unsetSpanLogs() {
    this.spanLogs = null;
  }

  /** Returns true if field spanLogs is set (has been assigned a value) and false otherwise */
  public boolean isSetSpanLogs() {
    return this.spanLogs != null;
  }

  public void setSpanLogsIsSet(boolean value) {
    if (!value) {
      this.spanLogs = null;
    }
  }

  public TSpanContext getContext() {
    return this.context;
  }

  public TSpan setContext(TSpanContext context) {
    this.context = context;
    return this;
  }

  public void unsetContext() {
    this.context = null;
  }

  /** Returns true if field context is set (has been assigned a value) and false otherwise */
  public boolean isSetContext() {
    return this.context != null;
  }

  public void setContextIsSet(boolean value) {
    if (!value) {
      this.context = null;
    }
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case OPERATION_NAME:
      if (value == null) {
        unsetOperationName();
      } else {
        setOperationName((String)value);
      }
      break;

    case START_TIME:
      if (value == null) {
        unsetStartTime();
      } else {
        setStartTime((Long)value);
      }
      break;

    case FINISH_TIME:
      if (value == null) {
        unsetFinishTime();
      } else {
        setFinishTime((Long)value);
      }
      break;

    case SPAN_TAGS:
      if (value == null) {
        unsetSpanTags();
      } else {
        setSpanTags((java.util.Map<String,String>)value);
      }
      break;

    case SPAN_LOGS:
      if (value == null) {
        unsetSpanLogs();
      } else {
        setSpanLogs((java.util.List<TSpanLog>)value);
      }
      break;

    case CONTEXT:
      if (value == null) {
        unsetContext();
      } else {
        setContext((TSpanContext)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case OPERATION_NAME:
      return getOperationName();

    case START_TIME:
      return getStartTime();

    case FINISH_TIME:
      return getFinishTime();

    case SPAN_TAGS:
      return getSpanTags();

    case SPAN_LOGS:
      return getSpanLogs();

    case CONTEXT:
      return getContext();

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case OPERATION_NAME:
      return isSetOperationName();
    case START_TIME:
      return isSetStartTime();
    case FINISH_TIME:
      return isSetFinishTime();
    case SPAN_TAGS:
      return isSetSpanTags();
    case SPAN_LOGS:
      return isSetSpanLogs();
    case CONTEXT:
      return isSetContext();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof TSpan)
      return this.equals((TSpan)that);
    return false;
  }

  public boolean equals(TSpan that) {
    if (that == null)
      return false;
    if (this == that)
      return true;

    boolean this_present_operationName = true && this.isSetOperationName();
    boolean that_present_operationName = true && that.isSetOperationName();
    if (this_present_operationName || that_present_operationName) {
      if (!(this_present_operationName && that_present_operationName))
        return false;
      if (!this.operationName.equals(that.operationName))
        return false;
    }

    boolean this_present_startTime = true;
    boolean that_present_startTime = true;
    if (this_present_startTime || that_present_startTime) {
      if (!(this_present_startTime && that_present_startTime))
        return false;
      if (this.startTime != that.startTime)
        return false;
    }

    boolean this_present_finishTime = true;
    boolean that_present_finishTime = true;
    if (this_present_finishTime || that_present_finishTime) {
      if (!(this_present_finishTime && that_present_finishTime))
        return false;
      if (this.finishTime != that.finishTime)
        return false;
    }

    boolean this_present_spanTags = true && this.isSetSpanTags();
    boolean that_present_spanTags = true && that.isSetSpanTags();
    if (this_present_spanTags || that_present_spanTags) {
      if (!(this_present_spanTags && that_present_spanTags))
        return false;
      if (!this.spanTags.equals(that.spanTags))
        return false;
    }

    boolean this_present_spanLogs = true && this.isSetSpanLogs();
    boolean that_present_spanLogs = true && that.isSetSpanLogs();
    if (this_present_spanLogs || that_present_spanLogs) {
      if (!(this_present_spanLogs && that_present_spanLogs))
        return false;
      if (!this.spanLogs.equals(that.spanLogs))
        return false;
    }

    boolean this_present_context = true && this.isSetContext();
    boolean that_present_context = true && that.isSetContext();
    if (this_present_context || that_present_context) {
      if (!(this_present_context && that_present_context))
        return false;
      if (!this.context.equals(that.context))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int hashCode = 1;

    hashCode = hashCode * 8191 + ((isSetOperationName()) ? 131071 : 524287);
    if (isSetOperationName())
      hashCode = hashCode * 8191 + operationName.hashCode();

    hashCode = hashCode * 8191 + org.apache.thrift.TBaseHelper.hashCode(startTime);

    hashCode = hashCode * 8191 + org.apache.thrift.TBaseHelper.hashCode(finishTime);

    hashCode = hashCode * 8191 + ((isSetSpanTags()) ? 131071 : 524287);
    if (isSetSpanTags())
      hashCode = hashCode * 8191 + spanTags.hashCode();

    hashCode = hashCode * 8191 + ((isSetSpanLogs()) ? 131071 : 524287);
    if (isSetSpanLogs())
      hashCode = hashCode * 8191 + spanLogs.hashCode();

    hashCode = hashCode * 8191 + ((isSetContext()) ? 131071 : 524287);
    if (isSetContext())
      hashCode = hashCode * 8191 + context.hashCode();

    return hashCode;
  }

  public int compareTo(TSpan other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = Boolean.valueOf(isSetOperationName()).compareTo(other.isSetOperationName());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetOperationName()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.operationName, other.operationName);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetStartTime()).compareTo(other.isSetStartTime());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetStartTime()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.startTime, other.startTime);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetFinishTime()).compareTo(other.isSetFinishTime());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetFinishTime()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.finishTime, other.finishTime);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetSpanTags()).compareTo(other.isSetSpanTags());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetSpanTags()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.spanTags, other.spanTags);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetSpanLogs()).compareTo(other.isSetSpanLogs());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetSpanLogs()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.spanLogs, other.spanLogs);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetContext()).compareTo(other.isSetContext());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetContext()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.context, other.context);
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

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("TSpan(");
    boolean first = true;

    sb.append("operationName:");
    if (this.operationName == null) {
      sb.append("null");
    } else {
      sb.append(this.operationName);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("startTime:");
    sb.append(this.startTime);
    first = false;
    if (!first) sb.append(", ");
    sb.append("finishTime:");
    sb.append(this.finishTime);
    first = false;
    if (isSetSpanTags()) {
      if (!first) sb.append(", ");
      sb.append("spanTags:");
      if (this.spanTags == null) {
        sb.append("null");
      } else {
        sb.append(this.spanTags);
      }
      first = false;
    }
    if (isSetSpanLogs()) {
      if (!first) sb.append(", ");
      sb.append("spanLogs:");
      if (this.spanLogs == null) {
        sb.append("null");
      } else {
        sb.append(this.spanLogs);
      }
      first = false;
    }
    if (!first) sb.append(", ");
    sb.append("context:");
    if (this.context == null) {
      sb.append("null");
    } else {
      sb.append(this.context);
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
      // it doesn't seem like you should have to do this, but java serialization is wacky, and doesn't call the default constructor.
      __isset_bitfield = 0;
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class TSpanStandardSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public TSpanStandardScheme getScheme() {
      return new TSpanStandardScheme();
    }
  }

  private static class TSpanStandardScheme extends org.apache.thrift.scheme.StandardScheme<TSpan> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, TSpan struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // OPERATION_NAME
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.operationName = iprot.readString();
              struct.setOperationNameIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // START_TIME
            if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
              struct.startTime = iprot.readI64();
              struct.setStartTimeIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // FINISH_TIME
            if (schemeField.type == org.apache.thrift.protocol.TType.I64) {
              struct.finishTime = iprot.readI64();
              struct.setFinishTimeIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 4: // SPAN_TAGS
            if (schemeField.type == org.apache.thrift.protocol.TType.MAP) {
              {
                org.apache.thrift.protocol.TMap _map26 = iprot.readMapBegin();
                struct.spanTags = new java.util.HashMap<String,String>(2*_map26.size);
                String _key27;
                String _val28;
                for (int _i29 = 0; _i29 < _map26.size; ++_i29)
                {
                  _key27 = iprot.readString();
                  _val28 = iprot.readString();
                  struct.spanTags.put(_key27, _val28);
                }
                iprot.readMapEnd();
              }
              struct.setSpanTagsIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 5: // SPAN_LOGS
            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
              {
                org.apache.thrift.protocol.TList _list30 = iprot.readListBegin();
                struct.spanLogs = new java.util.ArrayList<TSpanLog>(_list30.size);
                TSpanLog _elem31;
                for (int _i32 = 0; _i32 < _list30.size; ++_i32)
                {
                  _elem31 = new TSpanLog();
                  _elem31.read(iprot);
                  struct.spanLogs.add(_elem31);
                }
                iprot.readListEnd();
              }
              struct.setSpanLogsIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 6: // CONTEXT
            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
              struct.context = new TSpanContext();
              struct.context.read(iprot);
              struct.setContextIsSet(true);
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

    public void write(org.apache.thrift.protocol.TProtocol oprot, TSpan struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.operationName != null) {
        oprot.writeFieldBegin(OPERATION_NAME_FIELD_DESC);
        oprot.writeString(struct.operationName);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldBegin(START_TIME_FIELD_DESC);
      oprot.writeI64(struct.startTime);
      oprot.writeFieldEnd();
      oprot.writeFieldBegin(FINISH_TIME_FIELD_DESC);
      oprot.writeI64(struct.finishTime);
      oprot.writeFieldEnd();
      if (struct.spanTags != null) {
        if (struct.isSetSpanTags()) {
          oprot.writeFieldBegin(SPAN_TAGS_FIELD_DESC);
          {
            oprot.writeMapBegin(new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.STRING, struct.spanTags.size()));
            for (java.util.Map.Entry<String, String> _iter33 : struct.spanTags.entrySet())
            {
              oprot.writeString(_iter33.getKey());
              oprot.writeString(_iter33.getValue());
            }
            oprot.writeMapEnd();
          }
          oprot.writeFieldEnd();
        }
      }
      if (struct.spanLogs != null) {
        if (struct.isSetSpanLogs()) {
          oprot.writeFieldBegin(SPAN_LOGS_FIELD_DESC);
          {
            oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, struct.spanLogs.size()));
            for (TSpanLog _iter34 : struct.spanLogs)
            {
              _iter34.write(oprot);
            }
            oprot.writeListEnd();
          }
          oprot.writeFieldEnd();
        }
      }
      if (struct.context != null) {
        oprot.writeFieldBegin(CONTEXT_FIELD_DESC);
        struct.context.write(oprot);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class TSpanTupleSchemeFactory implements org.apache.thrift.scheme.SchemeFactory {
    public TSpanTupleScheme getScheme() {
      return new TSpanTupleScheme();
    }
  }

  private static class TSpanTupleScheme extends org.apache.thrift.scheme.TupleScheme<TSpan> {

    public void write(org.apache.thrift.protocol.TProtocol prot, TSpan struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol oprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      java.util.BitSet optionals = new java.util.BitSet();
      if (struct.isSetOperationName()) {
        optionals.set(0);
      }
      if (struct.isSetStartTime()) {
        optionals.set(1);
      }
      if (struct.isSetFinishTime()) {
        optionals.set(2);
      }
      if (struct.isSetSpanTags()) {
        optionals.set(3);
      }
      if (struct.isSetSpanLogs()) {
        optionals.set(4);
      }
      if (struct.isSetContext()) {
        optionals.set(5);
      }
      oprot.writeBitSet(optionals, 6);
      if (struct.isSetOperationName()) {
        oprot.writeString(struct.operationName);
      }
      if (struct.isSetStartTime()) {
        oprot.writeI64(struct.startTime);
      }
      if (struct.isSetFinishTime()) {
        oprot.writeI64(struct.finishTime);
      }
      if (struct.isSetSpanTags()) {
        {
          oprot.writeI32(struct.spanTags.size());
          for (java.util.Map.Entry<String, String> _iter35 : struct.spanTags.entrySet())
          {
            oprot.writeString(_iter35.getKey());
            oprot.writeString(_iter35.getValue());
          }
        }
      }
      if (struct.isSetSpanLogs()) {
        {
          oprot.writeI32(struct.spanLogs.size());
          for (TSpanLog _iter36 : struct.spanLogs)
          {
            _iter36.write(oprot);
          }
        }
      }
      if (struct.isSetContext()) {
        struct.context.write(oprot);
      }
    }

    public void read(org.apache.thrift.protocol.TProtocol prot, TSpan struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TTupleProtocol iprot = (org.apache.thrift.protocol.TTupleProtocol) prot;
      java.util.BitSet incoming = iprot.readBitSet(6);
      if (incoming.get(0)) {
        struct.operationName = iprot.readString();
        struct.setOperationNameIsSet(true);
      }
      if (incoming.get(1)) {
        struct.startTime = iprot.readI64();
        struct.setStartTimeIsSet(true);
      }
      if (incoming.get(2)) {
        struct.finishTime = iprot.readI64();
        struct.setFinishTimeIsSet(true);
      }
      if (incoming.get(3)) {
        {
          org.apache.thrift.protocol.TMap _map37 = new org.apache.thrift.protocol.TMap(org.apache.thrift.protocol.TType.STRING, org.apache.thrift.protocol.TType.STRING, iprot.readI32());
          struct.spanTags = new java.util.HashMap<String,String>(2*_map37.size);
          String _key38;
          String _val39;
          for (int _i40 = 0; _i40 < _map37.size; ++_i40)
          {
            _key38 = iprot.readString();
            _val39 = iprot.readString();
            struct.spanTags.put(_key38, _val39);
          }
        }
        struct.setSpanTagsIsSet(true);
      }
      if (incoming.get(4)) {
        {
          org.apache.thrift.protocol.TList _list41 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
          struct.spanLogs = new java.util.ArrayList<TSpanLog>(_list41.size);
          TSpanLog _elem42;
          for (int _i43 = 0; _i43 < _list41.size; ++_i43)
          {
            _elem42 = new TSpanLog();
            _elem42.read(iprot);
            struct.spanLogs.add(_elem42);
          }
        }
        struct.setSpanLogsIsSet(true);
      }
      if (incoming.get(5)) {
        struct.context = new TSpanContext();
        struct.context.read(iprot);
        struct.setContextIsSet(true);
      }
    }
  }

  private static <S extends org.apache.thrift.scheme.IScheme> S scheme(org.apache.thrift.protocol.TProtocol proto) {
    return (org.apache.thrift.scheme.StandardScheme.class.equals(proto.getScheme()) ? STANDARD_SCHEME_FACTORY : TUPLE_SCHEME_FACTORY).getScheme();
  }
}

