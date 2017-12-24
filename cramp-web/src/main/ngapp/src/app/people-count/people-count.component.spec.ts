import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PeopleCountComponent } from './people-count.component';

describe('PeopleCountComponent', () => {
  let component: PeopleCountComponent;
  let fixture: ComponentFixture<PeopleCountComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PeopleCountComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PeopleCountComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
