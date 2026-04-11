package com.monew.domain.user.event;

import java.util.UUID;

public record UserSoftDeletedEvent(UUID userId) {

}
