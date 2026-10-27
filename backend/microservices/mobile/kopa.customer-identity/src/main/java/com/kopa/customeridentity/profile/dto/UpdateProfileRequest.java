package com.kopa.customeridentity.profile.dto;

/** Only the fields a customer may self-edit - msisdn/custType/birthDay/status are KTRN-owned or
 *  identity-defining and aren't editable here. */
public record UpdateProfileRequest(
        String fullName,
        String email,
        String addressDetail
) {
}
