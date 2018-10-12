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


import java.util.Map;
import java.util.HashMap;
import org.apache.thrift.TEnum;

public enum TThreadState implements TEnum {
  NEW(0),
  RUNNABLE(1),
  BLOCKED(2),
  WAITING(3),
  TIMED_WAITING(4),
  TERMINATED(5);

  private final int value;

  private static Map<String, TThreadState> STATE_MAP = new HashMap<String, TThreadState>(6);

  static {
    for (TThreadState threadState : TThreadState.values()) {
      STATE_MAP.put(threadState.name(), threadState);
    }
  }

  private TThreadState(int value) {
    this.value = value;
  }

  /**
   * Get the integer value of this enum value, as defined in the Thrift IDL.
   */
  public int getValue() {
    return value;
  }

  public static TThreadState findByName(String name) {
    return STATE_MAP.get(name);
  }

  /**
   * Find a the enum type by its integer value, as defined in the Thrift IDL.
   * @return null if the value is not found.
   */
  public static TThreadState findByValue(int value) { 
    switch (value) {
      case 0:
        return NEW;
      case 1:
        return RUNNABLE;
      case 2:
        return BLOCKED;
      case 3:
        return WAITING;
      case 4:
        return TIMED_WAITING;
      case 5:
        return TERMINATED;
      default:
        return null;
    }
  }
}
