import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MovementDirectionComponent } from './movement-direction.component';

describe('MovementDirectionComponent', () => {
  let component: MovementDirectionComponent;
  let fixture: ComponentFixture<MovementDirectionComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MovementDirectionComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MovementDirectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
