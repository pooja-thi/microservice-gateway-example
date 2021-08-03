package com.library.user.web.rest.vm

import com.library.user.service.dto.AdminUserDTO

/**
 * View Model extending the [AdminUserDTO], which is meant to be used in the user management UI.
 */
class ManagedUserVM : AdminUserDTO() {

    override fun toString() = "ManagedUserVM{${super.toString()}}"
}
