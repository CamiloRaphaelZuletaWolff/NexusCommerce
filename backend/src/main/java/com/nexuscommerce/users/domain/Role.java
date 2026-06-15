package com.nexuscommerce.users.domain;

import com.nexuscommerce.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** An RBAC role (CUSTOMER, STORE_MANAGER, ADMIN). */
@Entity
@Table(name = "roles")
@Getter
@NoArgsConstructor
public class Role extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @Column(length = 255)
    private String description;

    public Role(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // Equality on the unique business key (role name) so Set<Role> de-duplicates correctly.
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o instanceof Role other && Objects.equals(name, other.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}
