package com.potados.geomms.common.base

import com.potados.geomms.common.base.interfaces.IsNavigationMember

/**
 * Base class for Fragment which is a member of [NavigationBasedActivity].
 */
abstract class NavigationBasedFragment : BaseFragment(),
    IsNavigationMember /* menuItemId() */
{

}