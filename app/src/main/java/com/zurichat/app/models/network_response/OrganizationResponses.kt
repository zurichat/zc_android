package com.zurichat.app.models.network_response

/*
* Jeffrey Orazulike [https://github.com/jeffreyorazulike]
* Created on 10/2/2021 at 10:20 PM 
*/

data class OrganizationData(
    val _id: String,
    val admins: Any,
    val created_at: String,
    val creator_email: String,
    val creator_id: String,
    val logo_url: String,
    val name: String,
    val plugins: Any,
    val settings: Any,
    val updated_at: String,
    val workspace_url: String
)

data class OrganizationCreatorResponse(
    val code: Int,
    val data: OrganizationCreatorIdData,
    val message: String
)

data class OrganizationCreatorIdData(
    val InsertedID: String
)

data class OrganizationCreator(
    val creator_email: String
)

data class Organization(
    val organizationData: OrganizationData,
    val message: String,
    val status: Int
)

data class UserOrganizationData(
    val _id: String,
    val imgs: ArrayList<String>,
    val isOwner: Boolean,
    val logo_url: String,
    val name: String,
    val no_of_members: Int,
    val workspace_url: String
)

data class OrganizationMembers(
    val code: Int, // 200 val message: String, val `data`: List<OrganizationMember>
)

data class UserOrganizationModel(val `data`: List<Data>, val message: String, val status: Int) {
    data class Data(
        val id: String,
        val imgs: List<String>,
        val isOwner: Boolean,
        val logo_url: String,
        val member_id: String,
        val name: String,
        val no_of_members: Int,
        val workspace_url: String
    )
}