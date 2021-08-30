package org.revcloud.vader.runner;

import com.force.swag.id.ID;
import de.cronn.reflection.util.TypedPropertyGetter;
import io.vavr.Function2;
import io.vavr.Tuple3;
import java.util.Collection;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.Nullable;

@Value
@FieldDefaults(level = AccessLevel.PACKAGE)
@Builder(buildMethodName = "prepare", builderMethodName = "toValidate", toBuilder = true)
public class IDConfig<ValidatableT, FailureT, EntityIdInfoT> {
  @Nullable Function2<ID, EntityIdInfoT, @NonNull Boolean> withIdValidator;

  @Singular("shouldHaveValidSFIdFormatOrFailWith")
  Collection<
          Tuple3<
              TypedPropertyGetter<ValidatableT, ID>, ? extends EntityIdInfoT, @Nullable FailureT>>
      shouldHaveValidSFIdFormatForAllOrFailWith;
}
