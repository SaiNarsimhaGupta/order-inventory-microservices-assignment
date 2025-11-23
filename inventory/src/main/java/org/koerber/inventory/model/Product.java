package org.koerber.inventory.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.koerber.inventory.enums.HandlerType;
import org.koerber.inventory.enums.ProductCategory;

/** Product Entity Each product can have multiple batches. */
@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "product_code", unique = true, nullable = false, length = 50)
  private String productCode;

  @Column(name = "name", nullable = false, length = 200)
  private String name;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ProductCategory category;

  @Column(name = "minimum_stock")
  private Integer minimumStock;

  @Enumerated(EnumType.STRING)
  @Column(name = "handler_type", nullable = false)
  @Builder.Default
  private HandlerType handlerType = HandlerType.STANDARD;

  @CreationTimestamp private LocalDateTime createdAt;
  @UpdateTimestamp private LocalDateTime updatedAt;
}
