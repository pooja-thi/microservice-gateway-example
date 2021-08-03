package com.library.publication.web.rest.vm

import com.library.publication.service.dto.AdminUserDTO

/**
 * View Model extending the [AdminUserDTO], which is meant to be used in the user management UI.
 */
class ManagedUserVM : AdminUserDTO() {

    override fun toString() = "ManagedUserVM{${super.toString()}}"
}
