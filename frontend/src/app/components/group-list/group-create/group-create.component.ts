import {Component, OnInit} from '@angular/core';
import {GroupService} from "../../../services/group.service";
import {ActivatedRoute, Router} from "@angular/router";
import {ToastrService} from 'ngx-toastr';
import {NgForm, NgModel} from '@angular/forms';
import {GroupDto} from "../../../dtos/group";
import { BudgetDto } from '../../../dtos/budget';
import {UserSelection} from "../../../dtos/user";
import {map, Observable} from "rxjs";
import {UserService} from "../../../services/user.service";
import {FriendshipService} from "../../../services/friendship.service";
import {AutoCompleteCompleteEvent, AutoCompleteSelectEvent} from "primeng/autocomplete";
import {ConfirmationService, MessageService} from "primeng/api";
import {AuthService} from "../../../services/auth.service";


export enum GroupCreateEditMode {
  create,
  edit,
  info,
}

@Component({
  selector: 'app-group-create',
  templateUrl: './group-create.component.html',
  styleUrl: './group-create.component.scss'
})
export class GroupCreateComponent implements OnInit {
  mode: GroupCreateEditMode = GroupCreateEditMode.create;

  group: GroupDto = {
    groupName: '',
    members: []
  };
  dummyMemberSelectionModel: unknown; // Just needed for the autocomplete
  filteredFriends: any[] | undefined;
  friends: any[] | undefined;

  filteredFriendsEdit: any[] | undefined;
  friendsEdit: any[] | undefined;

  protected membersEmails: string[] = [];
  protected membersEmailsEdit: string[] = [];

  constructor(
    private service: GroupService,
    protected userService: UserService,
    private friendshipService: FriendshipService,
    private router: Router,
    private route: ActivatedRoute,
    private messageService: MessageService,
    protected authService: AuthService,
    private confirmationService: ConfirmationService,
  ) {
  }

  public get heading(): string {
    switch (this.mode) {
      case GroupCreateEditMode.create:
        return 'Create New Group';
      case GroupCreateEditMode.edit:
        return 'Edit Group';
      case GroupCreateEditMode.info:
        return 'Group Info';
      default:
        return '?';
    }
  }

  public get modalButtonText(): string {
    switch (this.mode) {
      case GroupCreateEditMode.create:
        return 'Do you want to cancel the creation of the group?';
      case GroupCreateEditMode.edit:
        return 'Do you want to remove your changes?';
      case GroupCreateEditMode.info:
        return '?';
      default:
        return '?';
    }
  }
  private get modalButtonCanceledText(): string {
    switch (this.mode) {
      case GroupCreateEditMode.create:
        return 'Group creation canceled';
      case GroupCreateEditMode.edit:
        return 'Edit of group canceled';
      default:
        return '?';
    }
  }

  get modeIsCreate(): boolean {
    return this.mode === GroupCreateEditMode.create;
  }

  get modeIsEdit(): boolean {
    return this.mode === GroupCreateEditMode.edit;
  }

  get modeIsInfo(): boolean {
    return this.mode === GroupCreateEditMode.info;
  }

  private get modeActionFinished(): string {
    switch (this.mode) {
      case GroupCreateEditMode.create:
        return 'created';
      case GroupCreateEditMode.edit:
        return 'edited';
      default:
        return '?';
    }
  }

  ngOnInit(): void {
    this.friendshipService.getFriends().subscribe({
      next: data => {
        this.friends = data;
        this.friends.sort((a, b) => a.localeCompare(b));
        if (this.modeIsEdit) {
          this.friendsEdit = data.filter(friend => !this.membersEmails.includes(friend));
          this.friendsEdit.sort((a, b) => a.localeCompare(b));
        }
      },
      error: error => {
        if (error && error.error && error.error.errors) {
          for (let i = 0; i < error.error.errors.length; i++) {
            this.messageService.add({severity:'error', summary:'Error', detail:`${error.error.errors[i]}`});
          }
        } else if (error && error.error && error.error.message) {
          this.messageService.add({severity:'error', summary:'Error', detail:`${error.error.message}`});
        } else if (error && error.error && error.error.detail) {
          this.messageService.add({severity:'error', summary:'Error', detail:`${error.error.detail}`});
        } else {
          console.error('Error getting friends', error);
          this.messageService.add({severity:'error', summary:'Error', detail:`Getting friends did not work!`});
        }
      }
    });


    this.route.data.subscribe(data => {
      this.mode = data.mode;
    });

    let emailString = this.authService.getEmail();
    if(emailString === null) {
      this.messageService.add({severity:'error', summary:'Error', detail:`You need to be logged in to create a group. Please logout and login again.`});
      return;
    }

    this.membersEmails.push(this.authService.getEmail());

    if (!this.modeIsCreate) {
      this.getGroup();
    }
  }
  getGroup(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));

    this.service.getById(id)
      .subscribe( {
        next: data => {
          this.group = data;
          data.members.forEach(member => {
            this.membersEmails.push(member);
          });
          this.membersEmailsEdit = [];
        },
        error: error => {
          if (error && error.error && error.error.errors) {
            for (let i = 0; i < error.error.errors.length; i++) {
              this.messageService.add({severity:'error', summary:'Error', detail:`${error.error.errors[i]}`});
            }
          } else if (error && error.error && error.error.message) {
            this.messageService.add({severity:'error', summary:'Error', detail:`${error.error.message}`});
          } else if (error && error.error && error.error.detail) {
            this.messageService.add({severity:'error', summary:'Error', detail:`${error.error.detail}`});
          } else {
            console.error('Error getting group', error);
            this.messageService.add({severity:'error', summary:'Error', detail:`Getting group did not work!`});
          }
        }
      })
  }

  public onSubmit(form: NgForm): void {
    if (this.modeIsInfo) {
      //this.router.navigate([`groups/${(this.route.snapshot.paramMap.get('id'))}/edit`]);
      return;
    }

    var memberGroupSaved = JSON.parse(JSON.stringify(this.group.members));

    if (form.valid) {
      let observable: Observable<GroupDto>;
      switch (this.mode) {
        case GroupCreateEditMode.create:

          this.membersEmails.forEach(member => {
            this.group.members.push(member)
          });

          observable = this.service.create(this.group);
          break;
        case GroupCreateEditMode.edit:

          this.membersEmailsEdit.forEach(member => {
            this.group.members.push(member)
          });

          observable = this.service.update(this.group);
          break;
        default:
          console.error('Unknown GroupCreateEditMode', this.mode);
          return;
      }
      observable.subscribe({
        next: data => {
          this.messageService.add({severity:'success', summary:'Success', detail:`Group ${this.group.groupName} successfully ${this.modeActionFinished}.`});
          if (this.group.id) {
            this.router.navigate(['/group/' + this.group.id]);
          } else if (data.id) {
            this.router.navigate(['/group/' + data.id]);
          } else {
            this.router.navigate(['/home/groups']);
          }
        },
        error: error => {
          this.group.members = memberGroupSaved;
          if (error && error.error && error.error.errors) {
            //this.notification.error(`${error.error.errors.join('. \n')}`);
            for (let i = 0; i < error.error.errors.length; i++) {
              this.messageService.add({severity:'error', summary:'Error', detail:`${error.error.errors[i]}`});
            }
          } else if (error && error.error && error.error.message) { // if no detailed error explanation exists. Give a more general one if available.
            this.messageService.add({severity:'error', summary:'Error', detail:`${error.error.message}`});
          } else if (error && error.error.detail) {
            this.messageService.add({severity:'error', summary:'Error', detail:`${error.error.detail}`});
          } else {
            switch (this.mode) {
              case GroupCreateEditMode.create:
                console.error('Error creating group', error);
                this.messageService.add({severity:'error', summary:'Error', detail:`Creation of group did not work!`});
                break;
              case GroupCreateEditMode.edit:
                console.error('Error editing group', error);
                this.messageService.add({severity:'error', summary:'Error', detail:`Edit of group did not work!`});
                break;
              default:
                console.error('Unknown GroupCreateEditMode. Operation did not work!', this.mode);
            }
          }
        }
      });
    }
  }


  public addMember(member: AutoCompleteSelectEvent) {
      setTimeout(() => {
        this.currentlySelected = ""
      });

      if (!member.value) return;
      if (this.membersEmails.includes(member.value)) {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: `${member.value} is already in participant list`
        });
        return;
      }
    this.membersEmails.push(member.value);
  }

  public addMemberEdit(member: AutoCompleteSelectEvent) {
    setTimeout(() => {
      this.currentlySelected = ""
    });

    if (!member.value) return;
    if (this.membersEmailsEdit.includes(member.value)) {
      this.messageService.add({
        severity: 'error',
        summary: 'Error',
        detail: `${member.value} is already in participant list`
      });
      return;
    }
    this.membersEmailsEdit.push(member.value);
  }

  public removeMember(index: number) {
    if (this.authService.getEmail() == this.membersEmails[index]) {
      this.messageService.add({severity:'error', summary:'Error', detail:`You can't remove yourself from the group.`});
      return;
    }

    this.membersEmails.splice(index, 1);
  }

  public removeMemberEdit(index: number) {
    if (this.authService.getEmail() == this.membersEmailsEdit[index]) {
      this.messageService.add({severity:'error', summary:'Error', detail:`You can't remove yourself from the group.`});
      return;
    }

    this.membersEmailsEdit.splice(index, 1);
  }

  filterMembers(event: AutoCompleteCompleteEvent) {

    let filtered: any[] = [];
    let query = event.query;

    for (let i = 0; i < (this.friends as any[]).length; i++) {
      let friend = (this.friends as any[])[i];
      if (friend.toLowerCase().indexOf(query.toLowerCase()) == 0) {
        filtered.push(friend);
      }
    }

    this.filteredFriends = filtered;
  }

  filterMembersEdit(event: AutoCompleteCompleteEvent) {

    let filtered: any[] = [];
    let query = event.query;

    for (let i = 0; i < (this.friendsEdit as any[]).length; i++) {
      let friend = (this.friendsEdit as any[])[i];
      if (friend.toLowerCase().indexOf(query.toLowerCase()) == 0) {
        filtered.push(friend);
      }
    }

    this.filteredFriendsEdit = filtered;
  }

  getMembersEmail(): string[] {
    return this.membersEmails;
  }


  getSortedGroupMembersEmail(): string[] {
    return this.group.members.sort((a, b) => a.localeCompare(b));
  }

  // return only the members that are not in the group yet
  getMembersEmailEdit(): string[] {
    return this.membersEmailsEdit;
    // return this.membersEmails.filter(member => !this.group.members.includes(member));
  }

  protected readonly GroupCreateEditMode = GroupCreateEditMode;
  currentlySelected: any;

  goBack($event: MouseEvent) {
    this.confirmationService.confirm({
      target: event.target as EventTarget,
      message: this.modalButtonText,
      header: 'Cancel Confirmation',
      icon: 'pi pi-info-circle',
      acceptButtonStyleClass:"p-button-danger p-button-text",
      rejectButtonStyleClass:"p-button-text p-button-text",
      acceptIcon:"none",
      rejectIcon:"none",

      accept: () => {
        this.messageService.add({ severity: 'info', summary: 'Confirmed', detail: this.modalButtonCanceledText });
        if(this.mode === GroupCreateEditMode.create) {
          this.router.navigate(['/home/groups']);
          return;
        }
        this.router.navigate(['/group/' + this.group.id]);
      },
      reject: () => {
      }
    });
  }
}
