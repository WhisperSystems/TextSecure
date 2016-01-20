// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: CompressedInitiateSignal.proto

package org.privatechats.redphone.signaling.signals;

public final class CompressedInitiateSignalProtocol {
  private CompressedInitiateSignalProtocol() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
  }
  public interface CompressedInitiateSignalOrBuilder
      extends com.google.protobuf.MessageOrBuilder {
    
    // optional string initiator = 1;
    boolean hasInitiator();
    String getInitiator();
    
    // optional uint64 sessionId = 2;
    boolean hasSessionId();
    long getSessionId();
    
    // optional uint32 port = 3;
    boolean hasPort();
    int getPort();
    
    // optional string serverName = 4;
    boolean hasServerName();
    String getServerName();
    
    // optional uint32 version = 5;
    boolean hasVersion();
    int getVersion();
  }
  public static final class CompressedInitiateSignal extends
      com.google.protobuf.GeneratedMessage
      implements CompressedInitiateSignalOrBuilder {
    // Use CompressedInitiateSignal.newBuilder() to construct.
    private CompressedInitiateSignal(Builder builder) {
      super(builder);
    }
    private CompressedInitiateSignal(boolean noInit) {}
    
    private static final CompressedInitiateSignal defaultInstance;
    public static CompressedInitiateSignal getDefaultInstance() {
      return defaultInstance;
    }
    
    public CompressedInitiateSignal getDefaultInstanceForType() {
      return defaultInstance;
    }
    
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return org.privatechats.redphone.signaling.signals.CompressedInitiateSignalProtocol.internal_static_redphone_CompressedInitiateSignal_descriptor;
    }
    
    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return org.privatechats.redphone.signaling.signals.CompressedInitiateSignalProtocol.internal_static_redphone_CompressedInitiateSignal_fieldAccessorTable;
    }
    
    private int bitField0_;
    // optional string initiator = 1;
    public static final int INITIATOR_FIELD_NUMBER = 1;
    private java.lang.Object initiator_;
    public boolean hasInitiator() {
      return ((bitField0_ & 0x00000001) == 0x00000001);
    }
    public String getInitiator() {
      java.lang.Object ref = initiator_;
      if (ref instanceof String) {
        return (String) ref;
      } else {
        com.google.protobuf.ByteString bs = 
            (com.google.protobuf.ByteString) ref;
        String s = bs.toStringUtf8();
        if (com.google.protobuf.Internal.isValidUtf8(bs)) {
          initiator_ = s;
        }
        return s;
      }
    }
    private com.google.protobuf.ByteString getInitiatorBytes() {
      java.lang.Object ref = initiator_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8((String) ref);
        initiator_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    
    // optional uint64 sessionId = 2;
    public static final int SESSIONID_FIELD_NUMBER = 2;
    private long sessionId_;
    public boolean hasSessionId() {
      return ((bitField0_ & 0x00000002) == 0x00000002);
    }
    public long getSessionId() {
      return sessionId_;
    }
    
    // optional uint32 port = 3;
    public static final int PORT_FIELD_NUMBER = 3;
    private int port_;
    public boolean hasPort() {
      return ((bitField0_ & 0x00000004) == 0x00000004);
    }
    public int getPort() {
      return port_;
    }
    
    // optional string serverName = 4;
    public static final int SERVERNAME_FIELD_NUMBER = 4;
    private java.lang.Object serverName_;
    public boolean hasServerName() {
      return ((bitField0_ & 0x00000008) == 0x00000008);
    }
    public String getServerName() {
      java.lang.Object ref = serverName_;
      if (ref instanceof String) {
        return (String) ref;
      } else {
        com.google.protobuf.ByteString bs = 
            (com.google.protobuf.ByteString) ref;
        String s = bs.toStringUtf8();
        if (com.google.protobuf.Internal.isValidUtf8(bs)) {
          serverName_ = s;
        }
        return s;
      }
    }
    private com.google.protobuf.ByteString getServerNameBytes() {
      java.lang.Object ref = serverName_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8((String) ref);
        serverName_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    
    // optional uint32 version = 5;
    public static final int VERSION_FIELD_NUMBER = 5;
    private int version_;
    public boolean hasVersion() {
      return ((bitField0_ & 0x00000010) == 0x00000010);
    }
    public int getVersion() {
      return version_;
    }
    
    private void initFields() {
      initiator_ = "";
      sessionId_ = 0L;
      port_ = 0;
      serverName_ = "";
      version_ = 0;
    }
    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized != -1) return isInitialized == 1;
      
      memoizedIsInitialized = 1;
      return true;
    }
    
    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      getSerializedSize();
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        output.writeBytes(1, getInitiatorBytes());
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        output.writeUInt64(2, sessionId_);
      }
      if (((bitField0_ & 0x00000004) == 0x00000004)) {
        output.writeUInt32(3, port_);
      }
      if (((bitField0_ & 0x00000008) == 0x00000008)) {
        output.writeBytes(4, getServerNameBytes());
      }
      if (((bitField0_ & 0x00000010) == 0x00000010)) {
        output.writeUInt32(5, version_);
      }
      getUnknownFields().writeTo(output);
    }
    
    private int memoizedSerializedSize = -1;
    public int getSerializedSize() {
      int size = memoizedSerializedSize;
      if (size != -1) return size;
    
      size = 0;
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        size += com.google.protobuf.CodedOutputStream
          .computeBytesSize(1, getInitiatorBytes());
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        size += com.google.protobuf.CodedOutputStream
          .computeUInt64Size(2, sessionId_);
      }
      if (((bitField0_ & 0x00000004) == 0x00000004)) {
        size += com.google.protobuf.CodedOutputStream
          .computeUInt32Size(3, port_);
      }
      if (((bitField0_ & 0x00000008) == 0x00000008)) {
        size += com.google.protobuf.CodedOutputStream
          .computeBytesSize(4, getServerNameBytes());
      }
      if (((bitField0_ & 0x00000010) == 0x00000010)) {
        size += com.google.protobuf.CodedOutputStream
          .computeUInt32Size(5, version_);
      }
      size += getUnknownFields().getSerializedSize();
      memoizedSerializedSize = size;
      return size;
    }
    
    private static final long serialVersionUID = 0L;
    @java.lang.Override
    protected java.lang.Object writeReplace()
        throws java.io.ObjectStreamException {
      return super.writeReplace();
    }
    
    public static org.privatechats.redphone.signaling.signals.CompressedInitiateSignalProtocol.CompressedInitiateSignal parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data).buildParsed();
    }
    public static org.privatechats.redphone.signaling.signals.CompressedInitiateSignalProtocol.CompressedInitiateSignal parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data, extensionRegistry)
               .buildParsed();
    }
    public static org.privatechats.redphone.signaling.signals.CompressedInitiateSignalProtocol.CompressedInitiateSignal parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data).buildParsed();
    }
    public static org.privatechats.redphone.signaling.signals.CompressedInitiateSignalProtocol.CompressedInitiateSignal parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data, extensionRegistry)
               .buildParsed();
    }
    public static org.privatechats.redphone.signaling.signals.CompressedInitiateSignalProtocol.CompressedInitiateSignal parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input).buildParsed();
    }
    public static org.privatechats.redphone.signaling.signals.CompressedInitiateSignalProtocol.CompressedInitiateSignal parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input, extensionRegistry)
               .buildParsed();
    }
    public static org.privatechats.redphone.signaling.signals.CompressedInitiateSignalProtocol.CompressedInitiateSignal parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      Builder builder = newBuilder();
      if (builder.mergeDelimitedFrom(input)) {
        return builder.buildParsed();
      } else {
        return null;
      }
    }
    public static org.privatechats.redphone.signaling.signals.CompressedInitiateSignalProtocol.CompressedInitiateSignal parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      Builder builder = newBuilder();
      if (builder.mergeDelimitedFrom(input, extensionRegistry)) {
        return builder.buildParsed();
      } else {
        return null;
      }
    }
    public static org.privatechats.redphone.signaling.signals.CompressedInitiateSignalProtocol.CompressedInitiateSignal parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input).buildParsed();
    }
    public static org.privatechats.redphone.signaling.signals.CompressedInitiateSignalProtocol.CompressedInitiateSignal parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input, extensionRegistry)
               .buildParsed();
    }
    
    public static Builder newBuilder() { return Builder.create(); }
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder(org.privatechats.redphone.signaling.signals.CompressedInitiateSignalProtocol.CompressedInitiateSignal prototype) {
      return newBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() { return newBuilder(this); }
    
    @java.lang.Override
    protected Builder newBuilderForType(
        com.google.protobuf.GeneratedMessage.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    public static final class Builder extends
        com.google.protobuf.GeneratedMessage.Builder<Builder>
       implements org.privatechats.redphone.signaling.signals.CompressedInitiateSignalProtocol.CompressedInitiateSignalOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return org.privatechats.redphone.signaling.signals.CompressedInitiateSignalProtocol.internal_static_redphone_CompressedInitiateSignal_descriptor;
      }
      
      protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return org.privatechats.redphone.signaling.signals.CompressedInitiateSignalProtocol.internal_static_redphone_CompressedInitiateSignal_fieldAccessorTable;
      }
      
      // Construct using org.privatechats.redphone.signaling.signals.CompressedInitiateSignalProtocol.CompressedInitiateSignal.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }
      
      private Builder(BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
        }
      }
      private static Builder create() {
        return new Builder();
      }
      
      public Builder clear() {
        super.clear();
        initiator_ = "";
        bitField0_ = (bitField0_ & ~0x00000001);
        sessionId_ = 0L;
        bitField0_ = (bitField0_ & ~0x00000002);
        port_ = 0;
        bitField0_ = (bitField0_ & ~0x00000004);
        serverName_ = "";
        bitField0_ = (bitField0_ & ~0x00000008);
        version_ = 0;
        bitField0_ = (bitField0_ & ~0x00000010);
        return this;
      }
      
      public Builder clone() {
        return create().mergeFrom(buildPartial());
      }
      
      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return org.privatechats.redphone.signaling.signals.CompressedInitiateSignalProtocol.CompressedInitiateSignal.getDescriptor();
      }
      
      public org.privatechats.redphone.signaling.signals.CompressedInitiateSignalProtocol.CompressedInitiateSignal getDefaultInstanceForType() {
        return org.privatechats.redphone.signaling.signals.CompressedInitiateSignalProtocol.CompressedInitiateSignal.getDefaultInstance();
      }
      
      public org.privatechats.redphone.signaling.signals.CompressedInitiateSignalProtocol.CompressedInitiateSignal build() {
        org.privatechats.redphone.signaling.signals.CompressedInitiateSignalProtocol.CompressedInitiateSignal result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }
      
      private org.privatechats.redphone.signaling.signals.CompressedInitiateSignalProtocol.CompressedInitiateSignal buildParsed()
          throws com.google.protobuf.InvalidProtocolBufferException {
        org.privatechats.redphone.signaling.signals.CompressedInitiateSignalProtocol.CompressedInitiateSignal result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(
            result).asInvalidProtocolBufferException();
        }
        return result;
      }
      
      public org.privatechats.redphone.signaling.signals.CompressedInitiateSignalProtocol.CompressedInitiateSignal buildPartial() {
        org.privatechats.redphone.signaling.signals.CompressedInitiateSignalProtocol.CompressedInitiateSignal result = new org.privatechats.redphone.signaling.signals.CompressedInitiateSignalProtocol.CompressedInitiateSignal(this);
        int from_bitField0_ = bitField0_;
        int to_bitField0_ = 0;
        if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
          to_bitField0_ |= 0x00000001;
        }
        result.initiator_ = initiator_;
        if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
          to_bitField0_ |= 0x00000002;
        }
        result.sessionId_ = sessionId_;
        if (((from_bitField0_ & 0x00000004) == 0x00000004)) {
          to_bitField0_ |= 0x00000004;
        }
        result.port_ = port_;
        if (((from_bitField0_ & 0x00000008) == 0x00000008)) {
          to_bitField0_ |= 0x00000008;
        }
        result.serverName_ = serverName_;
        if (((from_bitField0_ & 0x00000010) == 0x00000010)) {
          to_bitField0_ |= 0x00000010;
        }
        result.version_ = version_;
        result.bitField0_ = to_bitField0_;
        onBuilt();
        return result;
      }
      
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof org.privatechats.redphone.signaling.signals.CompressedInitiateSignalProtocol.CompressedInitiateSignal) {
          return mergeFrom((org.privatechats.redphone.signaling.signals.CompressedInitiateSignalProtocol.CompressedInitiateSignal)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }
      
      public Builder mergeFrom(org.privatechats.redphone.signaling.signals.CompressedInitiateSignalProtocol.CompressedInitiateSignal other) {
        if (other == org.privatechats.redphone.signaling.signals.CompressedInitiateSignalProtocol.CompressedInitiateSignal.getDefaultInstance()) return this;
        if (other.hasInitiator()) {
          setInitiator(other.getInitiator());
        }
        if (other.hasSessionId()) {
          setSessionId(other.getSessionId());
        }
        if (other.hasPort()) {
          setPort(other.getPort());
        }
        if (other.hasServerName()) {
          setServerName(other.getServerName());
        }
        if (other.hasVersion()) {
          setVersion(other.getVersion());
        }
        this.mergeUnknownFields(other.getUnknownFields());
        return this;
      }
      
      public final boolean isInitialized() {
        return true;
      }
      
      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        com.google.protobuf.UnknownFieldSet.Builder unknownFields =
          com.google.protobuf.UnknownFieldSet.newBuilder(
            this.getUnknownFields());
        while (true) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              this.setUnknownFields(unknownFields.build());
              onChanged();
              return this;
            default: {
              if (!parseUnknownField(input, unknownFields,
                                     extensionRegistry, tag)) {
                this.setUnknownFields(unknownFields.build());
                onChanged();
                return this;
              }
              break;
            }
            case 10: {
              bitField0_ |= 0x00000001;
              initiator_ = input.readBytes();
              break;
            }
            case 16: {
              bitField0_ |= 0x00000002;
              sessionId_ = input.readUInt64();
              break;
            }
            case 24: {
              bitField0_ |= 0x00000004;
              port_ = input.readUInt32();
              break;
            }
            case 34: {
              bitField0_ |= 0x00000008;
              serverName_ = input.readBytes();
              break;
            }
            case 40: {
              bitField0_ |= 0x00000010;
              version_ = input.readUInt32();
              break;
            }
          }
        }
      }
      
      private int bitField0_;
      
      // optional string initiator = 1;
      private java.lang.Object initiator_ = "";
      public boolean hasInitiator() {
        return ((bitField0_ & 0x00000001) == 0x00000001);
      }
      public String getInitiator() {
        java.lang.Object ref = initiator_;
        if (!(ref instanceof String)) {
          String s = ((com.google.protobuf.ByteString) ref).toStringUtf8();
          initiator_ = s;
          return s;
        } else {
          return (String) ref;
        }
      }
      public Builder setInitiator(String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000001;
        initiator_ = value;
        onChanged();
        return this;
      }
      public Builder clearInitiator() {
        bitField0_ = (bitField0_ & ~0x00000001);
        initiator_ = getDefaultInstance().getInitiator();
        onChanged();
        return this;
      }
      void setInitiator(com.google.protobuf.ByteString value) {
        bitField0_ |= 0x00000001;
        initiator_ = value;
        onChanged();
      }
      
      // optional uint64 sessionId = 2;
      private long sessionId_ ;
      public boolean hasSessionId() {
        return ((bitField0_ & 0x00000002) == 0x00000002);
      }
      public long getSessionId() {
        return sessionId_;
      }
      public Builder setSessionId(long value) {
        bitField0_ |= 0x00000002;
        sessionId_ = value;
        onChanged();
        return this;
      }
      public Builder clearSessionId() {
        bitField0_ = (bitField0_ & ~0x00000002);
        sessionId_ = 0L;
        onChanged();
        return this;
      }
      
      // optional uint32 port = 3;
      private int port_ ;
      public boolean hasPort() {
        return ((bitField0_ & 0x00000004) == 0x00000004);
      }
      public int getPort() {
        return port_;
      }
      public Builder setPort(int value) {
        bitField0_ |= 0x00000004;
        port_ = value;
        onChanged();
        return this;
      }
      public Builder clearPort() {
        bitField0_ = (bitField0_ & ~0x00000004);
        port_ = 0;
        onChanged();
        return this;
      }
      
      // optional string serverName = 4;
      private java.lang.Object serverName_ = "";
      public boolean hasServerName() {
        return ((bitField0_ & 0x00000008) == 0x00000008);
      }
      public String getServerName() {
        java.lang.Object ref = serverName_;
        if (!(ref instanceof String)) {
          String s = ((com.google.protobuf.ByteString) ref).toStringUtf8();
          serverName_ = s;
          return s;
        } else {
          return (String) ref;
        }
      }
      public Builder setServerName(String value) {
        if (value == null) {
    throw new NullPointerException();
  }
  bitField0_ |= 0x00000008;
        serverName_ = value;
        onChanged();
        return this;
      }
      public Builder clearServerName() {
        bitField0_ = (bitField0_ & ~0x00000008);
        serverName_ = getDefaultInstance().getServerName();
        onChanged();
        return this;
      }
      void setServerName(com.google.protobuf.ByteString value) {
        bitField0_ |= 0x00000008;
        serverName_ = value;
        onChanged();
      }
      
      // optional uint32 version = 5;
      private int version_ ;
      public boolean hasVersion() {
        return ((bitField0_ & 0x00000010) == 0x00000010);
      }
      public int getVersion() {
        return version_;
      }
      public Builder setVersion(int value) {
        bitField0_ |= 0x00000010;
        version_ = value;
        onChanged();
        return this;
      }
      public Builder clearVersion() {
        bitField0_ = (bitField0_ & ~0x00000010);
        version_ = 0;
        onChanged();
        return this;
      }
      
      // @@protoc_insertion_point(builder_scope:redphone.CompressedInitiateSignal)
    }
    
    static {
      defaultInstance = new CompressedInitiateSignal(true);
      defaultInstance.initFields();
    }
    
    // @@protoc_insertion_point(class_scope:redphone.CompressedInitiateSignal)
  }
  
  private static com.google.protobuf.Descriptors.Descriptor
    internal_static_redphone_CompressedInitiateSignal_descriptor;
  private static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_redphone_CompressedInitiateSignal_fieldAccessorTable;
  
  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\036CompressedInitiateSignal.proto\022\010redpho" +
      "ne\"s\n\030CompressedInitiateSignal\022\021\n\tinitia" +
      "tor\030\001 \001(\t\022\021\n\tsessionId\030\002 \001(\004\022\014\n\004port\030\003 \001" +
      "(\r\022\022\n\nserverName\030\004 \001(\t\022\017\n\007version\030\005 \001(\rB" +
      "O\n+org.privatechats.redphone.signaling.s" +
      "ignalsB CompressedInitiateSignalProtocol"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
      new com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner() {
        public com.google.protobuf.ExtensionRegistry assignDescriptors(
            com.google.protobuf.Descriptors.FileDescriptor root) {
          descriptor = root;
          internal_static_redphone_CompressedInitiateSignal_descriptor =
            getDescriptor().getMessageTypes().get(0);
          internal_static_redphone_CompressedInitiateSignal_fieldAccessorTable = new
            com.google.protobuf.GeneratedMessage.FieldAccessorTable(
              internal_static_redphone_CompressedInitiateSignal_descriptor,
              new java.lang.String[] { "Initiator", "SessionId", "Port", "ServerName", "Version", },
              org.privatechats.redphone.signaling.signals.CompressedInitiateSignalProtocol.CompressedInitiateSignal.class,
              org.privatechats.redphone.signaling.signals.CompressedInitiateSignalProtocol.CompressedInitiateSignal.Builder.class);
          return null;
        }
      };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        }, assigner);
  }
  
  // @@protoc_insertion_point(outer_class_scope)
}
