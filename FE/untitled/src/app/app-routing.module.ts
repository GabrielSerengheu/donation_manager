import {RouterModule, Routes} from "@angular/router";
import {CommonModule} from "@angular/common";
import {NgModule} from "@angular/core";
import {UserCreationComponent} from "./user/components/user-creation/user-creation.component";

import {CreateDonatorComponent} from "./donator/components/createDonator/createdonator.component";
import {DonatorListComponent} from "./donator/components/donator-list/donator-list.component";
import {EditDonatorComponent} from "./donator/components/edit-donator/edit-donator.component";

const routes: Routes = [
  { path: 'donator/create', component: CreateDonatorComponent },
  { path: 'donator/edit', component: DonatorListComponent },
  { path: 'donator/edit/:id', component: EditDonatorComponent,  },
  { path: 'donator/delete', component: CreateDonatorComponent },
  { path: '', redirectTo: 'login', pathMatch: 'full'},
  /*{ path: '**', redirectTo: 'login', pathMatch: 'full'},*/
  { path: 'create-user', component: UserCreationComponent }
];

@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    RouterModule.forRoot(routes),
  ],
  exports: [
    RouterModule
  ]
})
export class AppRoutingModule {

}
