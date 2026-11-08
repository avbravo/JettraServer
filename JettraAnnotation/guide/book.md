# JettraAnnotation - Comprehensive Guide & Architecture Manual

## 1. Overview & Architecture
`JettraAnnotation` is the annotation processing and compile-time metadata generator framework for the Jettra platform. It uses Java 25 annotation processors (`javax.annotation.processing.Processor`) to generate high-performance type adapters, schema validators, OpenAPI schemas, and record-to-model converters at build time.

---

## 2. Key Features
- **Zero-Runtime Overhead**: Generates plain Java code (`JavaPoet`) during `javac` compilation.
- **Record Schema Reflection**: Scans `@FluxModel`, `@Entity`, and `@RecordSchema` to produce immutable data transfer wrappers.
- **REST & Security Metadata**: Generates route tables and RBAC role checks automatically.
- **Compile-Time Type Safety**: Fails fast if entity bindings or required record components are mismatched.

---

## 3. Installation
```xml
<dependency>
    <groupId>io.jettra</groupId>
    <artifactId>JettraAnnotation</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

---

## 4. Usage & Code Examples

### 4.1 Annotating Records for Automatic Code Generation
```java
package com.example.model;

import io.jettra.annotation.FluxModel;
import io.jettra.annotation.Id;
import io.jettra.annotation.Field;

@FluxModel(collection = "products", engine = "RECORDS")
public record Product(
    @Id String sku,
    @Field(name = "product_name") String name,
    @Field double price,
    @Field boolean inStock
) {}
```

During build, `JettraAnnotation` generates `ProductRepository`, `ProductDtoAdapter`, and UI CRUD widgets automatically.
